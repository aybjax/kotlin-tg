package db.migrations_products

import db.models.Courses_Mechanicum
import db.models.Processes_Mechanicum
import org.jetbrains.exposed.sql.SchemaUtils

object MechanicumMigration {
    /**
     * Drop and migrate tables of mechanicum
     */
    fun initMechanicumTables() {
        SchemaUtils.drop(Courses_Mechanicum, Processes_Mechanicum)
        SchemaUtils.create(Courses_Mechanicum, Processes_Mechanicum)
    }
}