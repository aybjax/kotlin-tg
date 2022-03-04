package mechanicum.db.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Courses: IntIdTable() {
    val type = varchar("type", length = 20)
    val name = varchar("name", length = 100)
    val description = text("description")
    val processes_count = ushort("processes_count")
    val category_id = integer("category_id")
    val category_name = varchar("category_name", length = 50)
}

class Course(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<Course>(Courses)
    var type by Courses.type
    var name by Courses.name
    var description by Courses.description
    var processes_count by Courses.processes_count
    var category_id by Courses.category_id
    var category_name by Courses.category_name
}