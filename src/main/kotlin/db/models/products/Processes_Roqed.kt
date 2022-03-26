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
object Processes_Roqed: IntIdTable() {
    val description = text("description")
    val detailing = text("detailing")
    val order = integer("order")
    val course = reference("course_id", Courses_Roqed).nullable()
}

/**
 * Processes Dao
 */
class  ProcessRoqedDao(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<ProcessRoqedDao>(Processes_Roqed) {
        /**
         * Create ProcessDao List for AWS processDTO and course to bind to
         */
        fun fromProcessesCourseId(awsProcessDtos: List<AwsProcessDto>, courseRoqedDao: CourseRoqedDao): List<ProcessRoqedDao>
        {
            var result = mutableListOf<ProcessRoqedDao>()

            transaction {
                awsProcessDtos.sortedBy { it.animation_start }
                    .forEachIndexed { idx, process ->
                        if(process.animation_start > 0) {
                            result.add(
                                ProcessRoqedDao.new {
                                    course = courseRoqedDao
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

    var description by Processes_Roqed.description
    var detailing by Processes_Roqed.detailing
    var order by Processes_Roqed.order
    var course by CourseRoqedDao optionalReferencedOn Processes_Roqed.course
}
