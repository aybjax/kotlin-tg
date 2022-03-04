package mechanicum.db.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Processes: IntIdTable() {
    val description = text("description")
    val detailing = text("detailing")
    val order = ushort("order")
}

class  Process(id: EntityID<Int>): IntEntity(id) {
    var description by Processes.description
    var detailing by Processes.detailing
    var order by Processes.order
}
