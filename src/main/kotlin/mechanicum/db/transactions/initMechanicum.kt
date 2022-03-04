package mechanicum.db.transactions

import mechanicum.db.models.Courses
import mechanicum.db.models.Processes
import org.jetbrains.exposed.sql.SchemaUtils

fun initMechanicumTables() {
    SchemaUtils.drop(Courses, Processes)
    SchemaUtils.create(Courses, Processes)
}