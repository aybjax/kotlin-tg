package db

import variables.DatabaseTelegramEnvVars
import db.models.Users
import db.migrations_products.MechanicumMigration.initMechanicumTables
import db.migrations_products.MechanicumSeed.seedMechanicumTables
import db.migrations_products.RoqedMigration.initRoqedTables
import db.migrations_products.RoqedSeed.seedRoqedTables
import db.models.GeoData
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseObject {
    fun migrateUser() {
        transaction {
            SchemaUtils.drop(Users)
            SchemaUtils.create(Users)
        }

    }

    fun migrateGeoData() {
        SchemaUtils.drop(GeoData)
        SchemaUtils.create(GeoData)
    }

    fun migrateDatabase() {
        transaction {
            migrateUser()
            migrateGeoData()
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
    fun dbConnect(omitMigration: Boolean = false) {
        Database.connect("jdbc:mysql://localhost:3306/${DatabaseTelegramEnvVars.DATABASE}?&serverTimezone=UTC",
            driver = "com.mysql.cj.jdbc.Driver",
            user = DatabaseTelegramEnvVars.USER, password = DatabaseTelegramEnvVars.PASSWORD,
        )
    }
}