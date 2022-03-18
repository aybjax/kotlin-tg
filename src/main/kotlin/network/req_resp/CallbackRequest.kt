package network.req_resp

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import db.models.User
import extensions.intOrDefault
import extensions.longOrDefault
import extensions.tryInt
import extensions.tryLong

/**
 * Main Request Object
 *  used in routers
 */
data class CallbackRequest(
    override val user: User,
    var route: String,
    var queries: Map<String, String>,
    override val bot: Bot,
    override val chatId: ChatId,
    var needPadding: Boolean = true,
): Request(user, bot, chatId){
    /**
     * Get query ?a=b as specific nullable type
     */
    inline fun <reified T> getQueryOrNull(key: String): T? {
        val result = queries[key] ?: ""

        return when(T::class.java) {
            String::class.java -> result as? T
            Int::class.java -> result.tryInt() as? T
            Long::class.java -> result.tryLong() as? T
            else -> throw Exception("Type is not supported")
        }
    }

    /**
     * Get query ?a=b as specific type or default
     */
    inline fun <reified T> getQuery(key: String): T {
        val result = queries[key] ?: ""

        println(Int::class)
        println(T::class)

        return when(T::class) {
            String::class -> result as T
            Int::class -> result.intOrDefault() as T
            Long::class -> result.longOrDefault() as T
            else -> throw Exception("Aybjax ${T::class} is not supported")
        }
    }

    /**
     * update current route and query
     */
    fun updateRouteQuery(routeQuery: String) {
        val (route, query) = parseRoute(routeQuery)

        this.route = route
        this.queries = query
    }

    override fun toString(): String {
        return "$route?" + queries.toSortedMap().map {
            return it.key + "=" + it.value
        }.joinToString(";")
    }

    companion object {
        /**
         * Create CallbackRequest with userId
         */
        fun fromCallbackUser(callbackQuery: String, userDto: User.About, bot: Bot, chatId: ChatId): CallbackRequest {
            val user = User.getUser(userDto)

            return fromCallback(callbackQuery, user, bot, chatId);
        }

        /**
         * Create CallbackRequest with User object
         */
        fun fromCallback(callbackQuery: String, user: User, bot: Bot, chatId: ChatId): CallbackRequest {
            val (route, query) = parseRoute(callbackQuery)
            var needPadding = true;

            if(
                route == "forward-mechanicum-courses" ||
                route == "backwards-mechanicum-courses" ||
                route == "choose-mechanicum-course-id" ||
                route == "mechanicum-search-name" ||
                route == "start-mechanicum-course"
            ) {
                needPadding = false
            }

            return CallbackRequest(
                user,
                route,
                query,
                bot,
                chatId,
                needPadding,
            )
        }

        /**
         * Parse a?b=c to route (a) and map(b to c)
         */
        fun parseRoute(route: String): Pair<String, Map<String, String>> {
            val routeQuery = route.split("?")

            val route = routeQuery[0]
            val query = try {
                routeQuery[1].split(";").map {
                    it.split("=")
                }.map {
                    it[0] to it[1]
                }.toMap()
            }
            catch (e: Exception) {
                emptyMap<String, String>()
            }

            return route to query
        }
    }
}