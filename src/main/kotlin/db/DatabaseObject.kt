package db

import containers.EnvVars
import db.models.Users
import db.migrations_products.MechanicumMigration.initMechanicumTables
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
    fun dbConnect(omitMigration: Boolean = false) {
        Database.connect("jdbc:mysql://localhost:3306/${EnvVars.DATABASE}?&serverTimezone=UTC",
            driver = "com.mysql.cj.jdbc.Driver",
            user = EnvVars.USER, password = EnvVars.PASSWORD,
        )
    }
}