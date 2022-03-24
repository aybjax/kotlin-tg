package db.models.mechanicum.db.models

import extensions.shrink
import db.models.mechanicum.dto.aws_course.CourseDto
import network.req_resp.RequestPage
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.properties.Delegates

/**
 * Courses table represented with Exposed
 */
object Courses: IntIdTable() {
    val wdId = integer("wd_id").uniqueIndex()
    val type = varchar("type", length = 20)
    val name = varchar("name", length = 100)
    val description = text("description")
    val processes_count = integer("processes_count")
    val category_id = integer("category_id")
    val category_name = varchar("category_name", length = 50)
}

/**
 * Courses Dao
 */
class CourseDao(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<CourseDao>(Courses) {
        /**
         * Create CourseDao from aws courseDao
         */
        fun fromCourse(courseDto: CourseDto): CourseDao?
        {
            var result: CourseDao? = null

            transaction {
                result = CourseDao.new {
                    wdId = courseDto.id
                    type = courseDto.type
                    name = courseDto.name.shrink()
                    description = courseDto.description?.shrink() ?: ""
                    processesCount = courseDto.processes_count
                    categoryId = courseDto.category_id
                    categoryName = courseDto.category_name?.shrink()
                }
            }

            return result
        }

        /**
         * Returns courses list, current page and number all pages
         */
        fun getCoursePageCount(nameLike: String? = null, page: RequestPage): CoursePageCount {
            var pageCount by Delegates.notNull<Long>()
            var localPage = page

            val courses = transaction {
                var query = if(nameLike.isNullOrBlank()) {
                    CourseDao.all()
                } else {
                    CourseDao.find {
                        Courses.name.lowerCase() like nameLike
                    }
                }

                pageCount = page.getTotalPageCount(query.count())

                if(page.value > pageCount) localPage = localPage makeLessThan pageCount

                query.limit(page.sqlLimit, page.sqlOffset).toList();
            }

            return CoursePageCount(
                courses,
                localPage,
                pageCount,
            )
        }
    }

    var wdId by Courses.wdId
    var type by Courses.type
    var name by Courses.name
    var description by Courses.description
    var processesCount by Courses.processes_count
    var categoryId by Courses.category_id
    var categoryName by Courses.category_name

    /**
     * used solely for getCoursePageCount
     */
    data class CoursePageCount(
        val courses: List<CourseDao>,
        val page: RequestPage,
        val pageCount: Long,
    )
}