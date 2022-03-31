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
    val jsonRouting = text("configurations")
    val jsonAbout = text("about")
    val jsonCompletion = text("completion")
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
        val routingAdapter: JsonAdapter<Routing> = Moshi.Builder().
                                                                    build().adapter(Routing::class.java)
        /**
         * Moshi adapter for configuration object
         */
        val aboutAdapter: JsonAdapter<About> = Moshi.Builder().build().adapter(About::class.java)
        /**
         * Moshi adapter for configuration object
         */
        val completionAdapter: JsonAdapter<Completion> = Moshi.Builder().build().adapter(Completion::class.java)

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
                        routing = Routing()
                        about = userDto
                        completion = Completion()
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
    var jsonRouting by Users.jsonRouting
    var jsonAbout by Users.jsonAbout
    var jsonCompletion by Users.jsonCompletion

    var routing: Routing?
        get() = routingAdapter.fromJson(jsonRouting)
        set(value) {
            jsonRouting = routingAdapter.toJson(value)
        }

    var about: About?
        get() = aboutAdapter.fromJson(jsonAbout)
        set(value) {
            jsonAbout = aboutAdapter.toJson(value)
        }

    var completion: Completion?
        get() = completionAdapter.fromJson(jsonCompletion)
        set(value) {
            jsonCompletion = completionAdapter.toJson(value)
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
    data class Routing(
        var previous_query: String? = null,
        var prev_page: Long? = null,
        var course_ids: List<Int>? = null,
        var searchName: String? = null,
        var previous_input: String? = null,
        var previous_input_route: String? = null,
        var is_previous_done: Boolean = false,
    )

    /**
     * Request session/cookie like class
     */
    @JsonClass(generateAdapter = true)
    data class Completion(
        var course_id: Int? = null,
        var next_process_order: Int? = null,
        var total_processes: Int? = null,
        var correct_processes: Int? = null,
        var longitude: Float? = null,
        var latitude: Float? = null,
        var processCompletions: MutableList<ProcessCompletion> = mutableListOf<ProcessCompletion>(),

        ) {
        @JsonClass(generateAdapter = true)
        data class ProcessCompletion(
            val process_order: Int,
            val process_name: String,
            var status: CompletionStatus,
            var comment: String?,
        )

        enum class CompletionStatus {
            DONE,
            FAIL,
            PENDING,
            PENDING_AFTER_FAIL,
        }
    }

    /**
     * Updates current (or if absent new) configuration and returns it
     */
    fun updateRouting(callback: (Routing) -> Routing): Routing? {
        return transaction {
            routing = callback(routing ?: Routing())

            routing
        }
    }

    /**
     * Updates current (or if absent new) configuration and returns it
     */
    fun updateCompletion(callback: (Completion) -> Completion): Completion? {
        return transaction {
            completion = callback(completion ?: Completion())

            completion
        }
    }
}