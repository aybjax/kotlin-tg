package dataclasses.request

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import db.models.User
import extensions.intOrDefault
import extensions.longOrDefault
import extensions.tryInt
import extensions.tryLong
import routes.enums.*


data class MechanicumCallbackRequest(
    override val user: User,
    override var route: Routes,
    override var queries: Map<String, String>,
    override val bot: Bot,
    override val chatId: ChatId,
    override val messageId: Long,
    override var needPadding: Boolean = true,
): CallbackRequest(user, route, queries, bot, chatId, messageId, needPadding)
data class RoqedCallbackRequest(
    override val user: User,
    override var route: Routes,
    override var queries: Map<String, String>,
    override val bot: Bot,
    override val chatId: ChatId,
    override val messageId: Long,
    override var needPadding: Boolean = true,
): CallbackRequest(user, route, queries, bot, chatId, messageId, needPadding)
data class DimedusCallbackRequest(
    override val user: User,
    override var route: Routes,
    override var queries: Map<String, String>,
    override val bot: Bot,
    override val chatId: ChatId,
    override val messageId: Long,
    override var needPadding: Boolean = true,
): CallbackRequest(user, route, queries, bot, chatId, messageId, needPadding)
data class AcademixCallbackRequest(
    override val user: User,
    override var route: Routes,
    override var queries: Map<String, String>,
    override val bot: Bot,
    override val chatId: ChatId,
    override val messageId: Long,
    override var needPadding: Boolean = true,
): CallbackRequest(user, route, queries, bot, chatId, messageId, needPadding)
data class CommonCallbackRequest(
    override val user: User,
    override var route: Routes,
    override var queries: Map<String, String>,
    override val bot: Bot,
    override val chatId: ChatId,
    override val messageId: Long,
    override var needPadding: Boolean = true,
): CallbackRequest(user, route, queries, bot, chatId, messageId, needPadding)

/**
 * Main Request Object
 *  used in routers
 */
sealed class CallbackRequest(
    override val user: User,
    open var route: Routes,
    open var queries: Map<String, String>,
    override val bot: Bot,
    override val chatId: ChatId,
    override val messageId: Long,
    open var needPadding: Boolean = true,
): Request(user, bot, chatId, messageId){
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


    inline fun updateRouteQuery(route: Routes, query: Map<String, String> = emptyMap()): CallbackRequest? {
        return when(route) {
            is MechanicumRoutes -> MechanicumCallbackRequest(
                    user = user,
                    route = route,
                    queries = query,
                    bot = bot,
                    chatId = chatId,
                    messageId = messageId,
                    needPadding = needPadding,
            )
            is RoqedRoutes -> RoqedCallbackRequest(
                    user = user,
                    route = route,
                    queries = query,
                    bot = bot,
                    chatId = chatId,
                    messageId = messageId,
                    needPadding = needPadding,
            )
            is DimedusRoutes -> DimedusCallbackRequest(
                    user = user,
                    route = route,
                    queries = query,
                    bot = bot,
                    chatId = chatId,
                    messageId = messageId,
                    needPadding = needPadding,
            )
            is AcademixRoutes -> AcademixCallbackRequest(
                    user = user,
                    route = route,
                    queries = query,
                    bot = bot,
                    chatId = chatId,
                    messageId = messageId,
                    needPadding = needPadding,
            )
            is CommonRoutes -> CommonCallbackRequest(
                    user = user,
                    route = route,
                    queries = query,
                    bot = bot,
                    chatId = chatId,
                    messageId = messageId,
                    needPadding = needPadding,
            )
            is EmptyRoutes -> null
        }
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
        fun fromCallbackUser(callbackQuery: String, userDto: User.About, bot: Bot, chatId: ChatId, messageId: Long): CallbackRequest? {
            val user = User.getUser(userDto)

            return fromCallback(callbackQuery, user, bot, chatId, messageId);
        }

        fun getRouteEnumFromString(route: String): Routes? {
            try {
                AcademixRoutes.valueOf(route)
            } catch (ex: Exception) {
                null
            }?.let {
                return it
            }

            try {
                CommonRoutes.valueOf(route)
            } catch (ex: Exception) {
                null
            }?.let {
                return it
            }

            try {
                DimedusRoutes.valueOf(route)
            } catch (ex: Exception) {
                null
            }?.let {
                return it
            }

            try {
                MechanicumRoutes.valueOf(route)
            } catch (ex: Exception) {
                null
            }?.let {
                return it
            }

            try {
                RoqedRoutes.valueOf(route)
            } catch (ex: Exception) {
                null
            }?.let {
                return it
            }

            return null
        }

        /**
         * Create CallbackRequest with User object
         */
        fun fromCallback(callbackQuery: String, user: User, bot: Bot, chatId: ChatId, messageId: Long): CallbackRequest? {
            val (route, query) = parseRoute(callbackQuery)
            val routeEnum = getRouteEnumFromString(route) ?: return null

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

            return when(routeEnum) {
                is AcademixRoutes -> AcademixCallbackRequest(
                    user,
                    routeEnum,
                    query,
                    bot,
                    chatId,
                    messageId,
                    needPadding,
                )
                is CommonRoutes -> CommonCallbackRequest(
                    user,
                    routeEnum,
                    query,
                    bot,
                    chatId,
                    messageId,
                    needPadding,
                )
                is DimedusRoutes -> DimedusCallbackRequest(
                    user,
                    routeEnum,
                    query,
                    bot,
                    chatId,
                    messageId,
                    needPadding,
                )
                is MechanicumRoutes -> MechanicumCallbackRequest(
                    user,
                    routeEnum,
                    query,
                    bot,
                    chatId,
                    messageId,
                    needPadding,
                )
                is RoqedRoutes -> RoqedCallbackRequest(
                    user,
                    routeEnum,
                    query,
                    bot,
                    chatId,
                    messageId,
                    needPadding,
                )
                is EmptyRoutes -> null
            }
        }

        /**
         * Parse a?b=c to route (a) and map(b to c)
         */
        fun parseRoute(route: String): Pair<String, Map<String, String>> {
            val routeQuery = route.split("?")

            val route = routeQuery[0]
            val query = try {
                routeQuery[1].split("&").map {
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