package mechanicum.db.models

import examples.orm.Cities
import examples.orm.City
import examples.orm.Users
import examples.orm.Users.nullable
import mechanicum.dto.aws_course.ProcessDto
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object Processes: IntIdTable() {
    val description = text("description")
    val detailing = text("detailing")
    val order = integer("order")
    val course = reference("course_id", Courses).nullable()
}

class  ProcessEntity(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<ProcessEntity>(Processes) {
        fun fromProcessesCourseId(processDtos: List<ProcessDto>, courseEntity: CourseEntity): List<ProcessEntity>
        {
            var result = mutableListOf<ProcessEntity>()

            transaction {
                processDtos.sortedBy { it.animation_start }
                    .forEachIndexed { idx, process ->
                        if(process.animation_start > 0) {
                            result.add(
                                ProcessEntity.new {
                                    course = courseEntity
                                    description = process.description
                                    detailing = process.detailing
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
    var course by CourseEntity optionalReferencedOn Processes.course
}
