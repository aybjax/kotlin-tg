package db.models

import com.github.kotlintelegrambot.entities.Chat
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
    val jsonAbout = text("about")
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
         * Moshi adapter for configuration object
         */
        val aboutAdapter: JsonAdapter<About> = Moshi.Builder().build().adapter(About::class.java)

        /**
         * Get User Dao with telegram chat id
         */
        fun getUser(userDto: About): User {
            val user = transaction {
                User.find {
                    Users.userId eq userDto.user_id
                }.firstOrNull()?.let {
                    updateAbout(userDto, it)

                    return@transaction it
                }

                return@transaction transaction {
                    User.new {
                        this.userId = userDto.user_id
                        configurations = Configurations()
                        about = userDto
                    }
                }
            }



            return user;
        }

        private fun updateAbout(about: About, user: User) {
            if(about != user.about) {
                transaction {
                    user.about = about
                }
            }
        }
    }

    var userId by Users.userId
    var jsonConfigurations by Users.jsonConfigurations
    var jsonAbout by Users.jsonAbout

    var configurations: Configurations?
        get() = configurationsAdapter.fromJson(jsonConfigurations)
        set(value) {
            jsonConfigurations = configurationsAdapter.toJson(value)
        }

    var about: About?
        get() = aboutAdapter.fromJson(jsonAbout)
        set(value) {
            jsonAbout = aboutAdapter.toJson(value)
        }

    /**
     * User info
     */
    @JsonClass(generateAdapter = true)
    data class About(
        val user_id: Long,
        val type: ChatType,
        val username: String,
        val firstName: String,
        val lastName: String,
    ) {
        companion object {
            fun fromChat(chat: Chat): About {
                return About(
                    user_id = chat.id,
                    type = ChatType.valueOf(chat.type.uppercase()),
                    username = chat.username ?: "",
                    firstName = chat.firstName ?: "",
                    lastName = chat.lastName ?: "",
                )
            }
        }
    }

    enum class ChatType {
        GROUP,
        PRIVATE
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