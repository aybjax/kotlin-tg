package db.migrations_products

import variables.products.RoqedAWS
import dataclasses.aws.AwsCoursesDto
import dataclasses.aws.AwsProcessesDto
import dataclasses.aws.S3BucketReader
import db.models.CourseRoqedDao
import db.models.ProcessRoqedDao
import kotlinx.coroutines.delay
import org.jetbrains.exposed.sql.transactions.transaction

object RoqedSeed {
    suspend fun seedRoqedTables(){
        val bucket = S3BucketReader(RoqedAWS.ROQED_REGION, RoqedAWS.ROQED_BUCKET)

        val result = bucket.getBucketObject<AwsCoursesDto>(RoqedAWS.ROQED_COURSES_FILE_FULL_PATH)
        result?.let { awsCourses ->
            awsCourses.course.forEach { course ->
                CourseRoqedDao.fromCourse(course)?.let { courseEntity ->
                    var isFetched = false

                    while(!isFetched) {
                        isFetched = try {
                            val courseDto = bucket.getBucketObject<AwsProcessesDto>(
                                RoqedAWS.ROQED_COURSE_DIR + courseEntity.wdId + RoqedAWS.JSON_EXTENSION
                            )

                            val processes = courseDto?.awsProcessDtos?.let { processes ->
                                ProcessRoqedDao.fromProcessesCourseId(processes, courseEntity)
                            }

                            transaction {
                                courseEntity.processesCount = processes?.size ?: -1
                            }

                            delay(1000)

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