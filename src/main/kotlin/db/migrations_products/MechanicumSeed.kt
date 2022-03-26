package db.migrations_products

import containers.products.MechanicumAWS
import dataclasses.aws.AwsCoursesDto
import dataclasses.aws.AwsProcessesDto
import dataclasses.aws.S3BucketReader
import db.models.CourseMechanicumDao
import db.models.ProcessMechanicumDao
import kotlinx.coroutines.delay
import org.jetbrains.exposed.sql.transactions.transaction

object MechanicumSeed {
    suspend fun seedMechanicumTables(){
        val bucket = S3BucketReader(MechanicumAWS.MECHANICUM_REGION, MechanicumAWS.MECHANICUM_BUCKET)

        val result = bucket.getBucketObject<AwsCoursesDto>(MechanicumAWS.MECHANICUM_COURSES_FILE_FULL_PATH)
        result?.let { awsCourses ->
            awsCourses.course.forEach { course ->
                CourseMechanicumDao.fromCourse(course)?.let { courseEntity ->
                    var isFetched = false

                    while(!isFetched) {
                        isFetched = try {
                            val courseDto = bucket.getBucketObject<AwsProcessesDto>(
                                MechanicumAWS.MECHANICUM_COURSE_DIR + courseEntity.wdId + MechanicumAWS.JSON_EXTENSION
                            )

                            val processes = courseDto?.awsProcessDtos?.let { processes ->
                                ProcessMechanicumDao.fromProcessesCourseId(processes, courseEntity)
                            }

                            transaction {
                                courseEntity.processesCount = processes?.size ?: -1
                            }

                            true
                        }
                        catch (e: Exception) {
                            println(e)
                            delay(1000);

                            false
                        }
                    }
                }
            }
        }
    }
}