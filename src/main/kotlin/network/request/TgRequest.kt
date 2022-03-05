package network.request

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import db.models.User

data class TgRequest(
    val user: User,
    val route: String,
    val queries: Map<String, String>,
    val bot: Bot,
    val chatid: ChatId,
) {
    fun getQuery(key: String) = queries[key]

    override fun toString(): String {
        return "$route?" + queries.toSortedMap().map {
            return it.key + "=" + it.value
        }.joinToString(";")
    }

    companion object {
        fun fromCallbackUser(callbackQuery: String, tg_user_id: Long, bot: Bot, chatId: ChatId): TgRequest {
            val user = User.getUser(tg_user_id)

            val routeQuery = callbackQuery.split("?")

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

            return TgRequest(
                user,
                route,
                query,
                bot,
                chatId
            )
        }
    }
}
