package mechanicum.db.models

import extensions.shrink
import mechanicum.dto.aws_course.CourseDto
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object Courses: IntIdTable() {
    val wd_id = integer("wd_id").uniqueIndex()
    val type = varchar("type", length = 20)
    val name = varchar("name", length = 100)
    val description = text("description")
    val processes_count = integer("processes_count")
    val category_id = integer("category_id")
    val category_name = varchar("category_name", length = 50)
}

class CourseEntity(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<CourseEntity>(Courses) {
        fun fromCourse(courseDto: CourseDto): CourseEntity?
        {
            var result: CourseEntity? = null

            transaction {
                result = CourseEntity.new {
                    wd_id = courseDto.id
                    type = courseDto.type
                    name = courseDto.name.shrink()
                    description = courseDto.description?.shrink() ?: ""
                    processes_count = courseDto.processes_count
                    category_id = courseDto.category_id
                    category_name = courseDto.category_name?.shrink()
                }
            }

            return result
        }
    }

    var wd_id by Courses.wd_id
    var type by Courses.type
    var name by Courses.name
    var description by Courses.description
    var processes_count by Courses.processes_count
    var category_id by Courses.category_id
    var category_name by Courses.category_name
}