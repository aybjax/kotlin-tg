package mechanicum.db.transactions

import mechanicum.db.models.Courses
import mechanicum.db.models.Processes
import org.jetbrains.exposed.sql.SchemaUtils

/**
 * Drop and migrate tables of mechanicum
 */
fun initMechanicumTables() {
    SchemaUtils.drop(Courses, Processes)
    SchemaUtils.create(Courses, Processes)
}