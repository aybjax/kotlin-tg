package db.models

import extensions.shrink
import dataclasses.aws.AwsProcessDto
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Process table with Exposed
 */
object Processes_Mechanicum: IntIdTable() {
    val description = text("description")
    val detailing = text("detailing")
    val order = integer("order")
    val course = reference("course_id", Courses_Mechanicum).nullable()
}

/**
 * Processes Dao
 */
class  ProcessMechanicumDao(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<ProcessMechanicumDao>(Processes_Mechanicum) {
        /**
         * Create ProcessDao List for AWS processDTO and course to bind to
         */
        fun fromProcessesCourseId(awsProcessDtos: List<AwsProcessDto>, courseMechanicumDao: CourseMechanicumDao): List<ProcessMechanicumDao>
        {
            var result = mutableListOf<ProcessMechanicumDao>()

            transaction {
                awsProcessDtos.sortedBy { it.animation_start }
                    .forEachIndexed { idx, process ->
                        if(process.animation_start > 0) {
                            result.add(
                                ProcessMechanicumDao.new {
                                    course = courseMechanicumDao
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

    var description by Processes_Mechanicum.description
    var detailing by Processes_Mechanicum.detailing
    var order by Processes_Mechanicum.order
    var course by CourseMechanicumDao optionalReferencedOn Processes_Mechanicum.course
}
