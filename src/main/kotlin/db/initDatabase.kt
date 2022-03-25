package db

import constants.EnvVars
import db.models.Users
import db.models.mechanicum.constants.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import db.models.mechanicum.db.models.CourseDao
import db.models.mechanicum.db.models.ProcessDao
import db.models.mechanicum.db.transactions.initMechanicumTables
import db.models.mechanicum.dto.AwsCourseDto
import db.models.mechanicum.dto.AwsCoursesDto
import network.aws.S3BucketReader
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun migrateDatabase() {
    transaction {
        SchemaUtils.drop(Users)
        SchemaUtils.create(Users)
        initMechanicumTables()
    }
}

fun seedDatabase() {
    runBlocking {
        val bucket = S3BucketReader(MECHANICUM_REGION, MECHANICUM_BUCKET)

        val result = bucket.getBucketObject<AwsCoursesDto>(MECHANICUM_COURSES_FILE_FULL_PATH)
        result?.let { awsCourses ->
            awsCourses.course.forEach { course ->
                CourseDao.fromCourse(course)?.let { courseEntity ->
                    var isFetched = false

                    while(!isFetched) {
                        isFetched = try {
                            val awsCourseDto = bucket.getBucketObject<AwsCourseDto>(
                                MECHANICUM_COURSE_DIR + courseEntity.wdId + JSON_EXTENSION
                            )

                            val processes = awsCourseDto?.processDtos?.let { processes ->
                                ProcessDao.fromProcessesCourseId(processes, courseEntity)
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

/**
 * Connects to database and
 *    Drops and migrates and pull data from AWS
 * if omitMigration, then does not alter table
 */
fun initDatabase(omitMigration: Boolean = false) {
    Database.connect("jdbc:mysql://localhost:3306/${EnvVars.TELEGRAM_DATABASE}?&serverTimezone=UTC",
        driver = "com.mysql.cj.jdbc.Driver",
        user = EnvVars.TELEGRAM_USER, password = EnvVars.TELEGRAM_PASSWORD,
    )
}
