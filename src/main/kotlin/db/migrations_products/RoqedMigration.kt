package db.migrations_products

import db.models.Courses_Mechanicum
import db.models.Courses_Roqed
import db.models.Processes_Mechanicum
import db.models.Processes_Roqed
import org.jetbrains.exposed.sql.SchemaUtils

object RoqedMigration {
    /**
     * Drop and migrate tables of mechanicum
     */
    fun initRoqedTables() {
        SchemaUtils.drop(Courses_Roqed, Processes_Roqed)
        SchemaUtils.create(Courses_Roqed, Processes_Roqed)
    }
}