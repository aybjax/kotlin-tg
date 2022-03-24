package db.models.mechanicum.db.transactions

import db.models.mechanicum.db.models.Courses
import db.models.mechanicum.db.models.Processes
import org.jetbrains.exposed.sql.SchemaUtils

/**
 * Drop and migrate tables of mechanicum
 */
fun initMechanicumTables() {
    SchemaUtils.drop(Courses, Processes)
    SchemaUtils.create(Courses, Processes)
}