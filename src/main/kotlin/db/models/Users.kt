package db.models

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object Users: IntIdTable() {
    val tg_user_id = long("tg_user_id").uniqueIndex()
    val json_configurations = text("configurations")
}

class User(id: EntityID<Int>): IntEntity(id)
{
    companion object: IntEntityClass<User>(Users) {
        val configurations_adapter = Moshi.Builder().build().adapter(Configurations::class.java)

        fun getUser(tg_id: Long): User {
            return transaction {
                User.find {
                    Users.tg_user_id eq tg_id
                }.firstOrNull()?.let {
                    return@transaction it
                }

                return@transaction transaction {
                    User.new {
                        tg_user_id = tg_id
                        configurations = Configurations()
                    }
                }
            }
        }
    }

    var tg_user_id by Users.tg_user_id
    var json_configurations by Users.json_configurations

    var configurations: Configurations?
        get() = configurations_adapter.fromJson(json_configurations)
        set(value) {
            json_configurations = configurations_adapter.toJson(value)
        }

    @JsonClass(generateAdapter = true)
    data class Configurations(
        var previous_query: String? = null,
        var prev_page: Long? = null,
        var course_min: Int? = null,
        var course_max: Int? = null,
        var course_id: Int? = null,
        var next_process_order: Int? = null,
        var total_processes: Int? = null,
        var correct_processes: Int? = null,
    )
}