package network.request

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import db.models.User
import extensions.intOrDefault
import extensions.longOrDefault
import extensions.tryInt
import extensions.tryLong

data class TgRequest(
    var type: RequestType,
    val user: User,
    var route: String,
    var queries: Map<String, String>,
    val bot: Bot,
    val chatid: ChatId,
) {
    inline fun <reified T> getQueryOrNull(key: String): T? {
        val result = queries[key] ?: ""

        return when(T::class.java) {
            String::class.java -> result as? T
            Int::class.java -> result.tryInt() as? T
            Long::class.java -> result.tryLong() as? T
            else -> throw Exception("Type is not supported")
        }
    }

    inline fun <reified T> getQuery(key: String): T {
        val result = queries[key] ?: ""

        return when(T::class.java) {
            String::class.java -> result as T
            Int::class.java -> result.intOrDefault() as T
            Long::class.java -> result.longOrDefault() as T
            else -> throw Exception("Type is not supported")
        }
    }

    fun updateRouteQuery(routeQuery: String, requestType: RequestType) {
        val (route, query) = parseRoute(routeQuery)

        this.route = route
        this.queries = query
        this.type = requestType
    }

    override fun toString(): String {
        return "$route?" + queries.toSortedMap().map {
            return it.key + "=" + it.value
        }.joinToString(";")
    }

    companion object {
        fun fromCallbackUser(type: RequestType, callbackQuery: String, tg_user_id: Long, bot: Bot, chatId: ChatId): TgRequest {
            val user = User.getUser(tg_user_id)

            val (route, query) = parseRoute(callbackQuery)

            return TgRequest(
                type,
                user,
                route,
                query,
                bot,
                chatId
            )
        }

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
