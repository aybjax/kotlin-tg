package db.models

import extensions.shrink
import dataclasses.aws.AwsCourseDto
import dataclasses.RequestPage
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
object Courses_Mechanicum: IntIdTable() {
    val wdId = integer("wd_id").uniqueIndex()
    val type = varchar("type", length = 20)
    val name = text("name")
    val description = text("description")
    val processes_count = integer("processes_count")
}

/**
 * Courses Dao
 */
class CourseMechanicumDao(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<CourseMechanicumDao>(Courses_Mechanicum) {
        /**
         * Create CourseDao from aws courseDao
         */
        fun fromCourse(awsCourseDto: AwsCourseDto): CourseMechanicumDao?
        {
            var result: CourseMechanicumDao? = null

            transaction {
                result = CourseMechanicumDao.new {
                    wdId = awsCourseDto.id
                    type = awsCourseDto.type
                    name = awsCourseDto.name.shrink()
                    description = awsCourseDto.description?.shrink() ?: ""
                    processesCount = awsCourseDto.processes_count
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
                    CourseMechanicumDao.all()
                } else {
                    CourseMechanicumDao.find {
                        Courses_Mechanicum.name.lowerCase() like nameLike
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

    var wdId by Courses_Mechanicum.wdId
    var type by Courses_Mechanicum.type
    var name by Courses_Mechanicum.name
    var description by Courses_Mechanicum.description
    var processesCount by Courses_Mechanicum.processes_count

    /**
     * used solely for getCoursePageCount
     */
    data class CoursePageCount(
        val courses: List<CourseMechanicumDao>,
        val page: RequestPage,
        val pageCount: Long,
    )
}