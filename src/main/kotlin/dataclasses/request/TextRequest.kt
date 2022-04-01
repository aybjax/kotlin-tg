package dataclasses.request

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import controllers_products.MechanicumController
import controllers_products.RoqedController
import dataclasses.RouteQueryPair
import dataclasses.queries
import db.models.User
import extensions.getFirstWord
import extensions.normalizedString
import routes.enums.CommonRoutes
import routes.enums.EmptyRoutes

/**
 * Request object for text and commands
 *  Converted to CallbackRequest
 */
class TextRequest(
    override val user: User,
    text: String,
    override val bot: Bot,
    override val chatId: ChatId,
    override val messageId: Long,
    override val type: RequestType,
): Request(user, bot, chatId, messageId, type)
{
    val text = text.normalizedString()
    private val greetingWords = listOf("start", "начать", "домой");

    /**
     * Convert to CallbackRequest
     */
    fun toCallbackRequest(): CallbackRequest? {
        val queryPair = getCallbackQuery();

        return CallbackRequest.fromCallback(
            queryPair.toQueryString(),
            user,
            bot,
            chatId,
            messageId,
            requestType = this.type,
        )
    }

    /**
     * Create route (a?b=c) dependent on text written
     */
    private fun getCallbackQuery(): RouteQueryPair {
        val firstWord = text.getFirstWord()
        val expectedRoute = CallbackRequest.getRouteEnumFromString(user.routing?.expectedQuery?.route ?: "") ?: EmptyRoutes

        if(firstWord.isEmpty()) return EmptyRoutes queries emptyMap()

        if(greetingWords.contains(firstWord)) return CommonRoutes.GREET_USER queries emptyMap();

        MechanicumController.textToCallbackQuery(expectedRoute, text, this)?.let { routeQueryPair ->
            user.updateRouting {
                it.expectedQuery = null

                it
            }

            return routeQueryPair
        }

        RoqedController.textToCallbackQuery(expectedRoute, text, this)?.let { routeQueryPair ->
            user.updateRouting {
                it.expectedQuery = null

                it
            }

            return routeQueryPair
        }

        user.routing?.expectedQuery?.let {
            writeButton("Комманда не понята.")
        }

        user.updateRouting {
            it.expectedQuery = null

            it
        }

        return EmptyRoutes queries emptyMap()
    }


    companion object {
        /**
         * Create TextRequest from text and User ID
         */
        fun fromTextUser(text: String, userDto: User.About, bot: Bot, chatId: ChatId, messageId: Long, type: RequestType): TextRequest {
            val user = User.getUser(userDto)

            return TextRequest(user, text, bot, chatId, messageId, type)
        }
    }
}
