package db

import containers.EnvVars
import db.models.Users
import containers.products.MechanicumAWS
import db.migrations_products.MechanicumMigration.initMechanicumTables
import db.models.CourseMechanicumDao
import db.models.ProcessMechanicumDao
import dataclasses.aws.AwsProcessesDto
import dataclasses.aws.AwsCoursesDto
import kotlinx.coroutines.delay
import dataclasses.aws.S3BucketReader
import db.migrations_products.MechanicumSeed.seedMechanicumTables
import db.migrations_products.RoqedMigration.initRoqedTables
import db.migrations_products.RoqedSeed.seedRoqedTables
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseObject {
    fun migrateDatabase() {
        transaction {
            SchemaUtils.drop(Users)
            SchemaUtils.create(Users)
            initMechanicumTables()
            initRoqedTables()
        }
    }

    suspend fun seedDatabase() {
        seedMechanicumTables()
        seedRoqedTables()
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
}