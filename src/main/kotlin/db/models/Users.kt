package db.models

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Users table represented with Exposed library
 */
object Users: IntIdTable() {
    val userId = long("tg_user_id").uniqueIndex()
    val jsonConfigurations = text("configurations")
}

/**
 * User Dao
 */
class User(id: EntityID<Int>): IntEntity(id)
{
    companion object: IntEntityClass<User>(Users) {
        /**
         * Moshi adapter for configuration object
         */
        val configurationsAdapter: JsonAdapter<Configurations> = Moshi.Builder().
                                                                    build().adapter(Configurations::class.java)

        /**
         * Get User Dao with telegram chat id
         */
        fun getUser(user_id: Long): User {
            return transaction {
                User.find {
                    Users.userId eq user_id
                }.firstOrNull()?.let {
                    return@transaction it
                }

                return@transaction transaction {
                    User.new {
                        this.userId = user_id
                        configurations = Configurations()
                    }
                }
            }
        }
    }

    var userId by Users.userId
    var jsonConfigurations by Users.jsonConfigurations

    var configurations: Configurations?
        get() = configurationsAdapter.fromJson(jsonConfigurations)
        set(value) {
            jsonConfigurations = configurationsAdapter.toJson(value)
        }

    /**
     * Request session/cookie like class
     */
    @JsonClass(generateAdapter = true)
    data class Configurations(
        var previous_query: String? = null,
        var prev_page: Long? = null,
        var course_ids: List<Int>? = null,
        var course_id: Int? = null,
        var next_process_order: Int? = null,
        var total_processes: Int? = null,
        var correct_processes: Int? = null,
        var searchName: String? = null,
    )

    /**
     * Updates current (or if absent new) configuration and returns it
     */
    fun updateConfiguration(callback: (Configurations) -> Configurations): Configurations? {
        return transaction {
            configurations = callback(configurations ?: Configurations())

            configurations
        }
    }
}