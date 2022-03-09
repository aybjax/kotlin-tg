package db

import db.models.Users
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mechanicum.constants.*
import mechanicum.db.models.CourseEntity
import mechanicum.db.models.ProcessEntity
import mechanicum.db.transactions.initMechanicumTables
import mechanicum.dto.AwsCourseDto
import mechanicum.dto.AwsCoursesDto
import network.aws.S3BucketReader
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun initDatabase(omitMigration: Boolean = false) {
    Database.connect("jdbc:mysql://localhost:3306/kotlintg?&serverTimezone=UTC", driver = "com.mysql.cj.jdbc.Driver",
        user = "root", password = dotenv()["ps"],
    )

    if(omitMigration) return;

    transaction {
        SchemaUtils.drop(Users)
        SchemaUtils.create(Users)
        initMechanicumTables()
    }

    runBlocking {
        val bucket = S3BucketReader(MECHANICUM_REGION, MECHANICUM_BUCKET)

        val result = bucket.getBucketObject<AwsCoursesDto>(MECHANICUM_COURSES_FILE_FULL_PATH)
        result?.let { awsCourses ->
            awsCourses.cours.forEach { course ->
                CourseEntity.fromCourse(course)?.let { courseEntity ->
                    var isFetched = false

                    while(!isFetched) {
                        isFetched = try {
                            val awsCourseDto = bucket.getBucketObject<AwsCourseDto>(
                                MECHANICUM_COURSE_DIR + courseEntity.wd_id + JSON_EXTENSION
                            )

                            val processes = awsCourseDto?.processDtos?.let { processes ->
                                ProcessEntity.fromProcessesCourseId(processes, courseEntity)
                            }

                            transaction {
                                courseEntity.processes_count = processes?.size ?: -1
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
