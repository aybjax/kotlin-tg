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
): Request(user, bot, chatId, messageId)
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
        )
    }

    /**
     * Create route (a?b=c) dependent on text written
     */
    private fun getCallbackQuery(): RouteQueryPair {
        val firstWord = text.getFirstWord()
        val previousQuery = CallbackRequest.getRouteEnumFromString(user.routing?.previous_query ?: "") ?: EmptyRoutes

        if(firstWord.isEmpty()) return EmptyRoutes queries emptyMap()

        if(greetingWords.contains(firstWord)) return CommonRoutes.GREET_USER queries emptyMap();

        MechanicumController.textToCallbackQuery(previousQuery, text, this)?.let {
            return it
        }

        RoqedController.textToCallbackQuery(previousQuery, text, this)?.let {
            return it
        }

        return EmptyRoutes queries emptyMap()
    }


    companion object {
        /**
         * Create TextRequest from text and User ID
         */
        fun fromTextUser(text: String, userDto: User.About, bot: Bot, chatId: ChatId, messageId: Long): TextRequest {
            val user = User.getUser(userDto)

            return TextRequest(user, text, bot, chatId, messageId)
        }
    }
}
