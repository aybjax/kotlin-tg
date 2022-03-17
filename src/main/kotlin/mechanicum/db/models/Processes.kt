package mechanicum.db.models

import extensions.shrink
import mechanicum.dto.aws_course.ProcessDto
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Process table with Exposed
 */
object Processes: IntIdTable() {
    val description = text("description")
    val detailing = text("detailing")
    val order = integer("order")
    val course = reference("course_id", Courses).nullable()
}

/**
 * Processes Dao
 */
class  ProcessDao(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<ProcessDao>(Processes) {
        /**
         * Create ProcessDao List for AWS processDTO and course to bind to
         */
        fun fromProcessesCourseId(processDtos: List<ProcessDto>, courseDao: CourseDao): List<ProcessDao>
        {
            var result = mutableListOf<ProcessDao>()

            transaction {
                processDtos.sortedBy { it.animation_start }
                    .forEachIndexed { idx, process ->
                        if(process.animation_start > 0) {
                            result.add(
                                ProcessDao.new {
                                    course = courseDao
                                    description = process.description.shrink()
                                    detailing = process.detailing.shrink()
                                    order = idx
                                }
                            )
                        }
                    }
            }

            return result
        }
    }

    var description by Processes.description
    var detailing by Processes.detailing
    var order by Processes.order
    var course by CourseDao optionalReferencedOn Processes.course
}
