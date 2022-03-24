import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.logging.LogLevel
import constants.MECHANICUM_TELEGRAM_TOKEN
import db.initDatabase
import db.models.User
import examples.telegramsamples.runDispatcherExample
import extensions.normalizedString
import network.req_resp.CallbackRequest
import network.req_resp.TextRequest
import network.route.Layout
import network.route.routeCallback
import org.jetbrains.exposed.sql.SqlExpressionBuilder.intToDecimal

fun main() {
    initDatabase(true)

    bot {
        token = MECHANICUM_TELEGRAM_TOKEN
        timeout = 30
        logLevel = LogLevel.Network.Body

        dispatch {
            text {
                val chat = message?.chat ?: return@text

                val userDto = User.About.fromChat(chat)

                val request = TextRequest.fromTextUser(
                    text, userDto, bot, ChatId.fromId(chat.id), message.messageId
                ).toCallbackRequest()

                routeCallback(request)
            }

            callbackQuery() {
                val chat = callbackQuery.message?.chat ?: return@callbackQuery
                val messageId = callbackQuery.message?.messageId ?: return@callbackQuery

                val userDto = User.About.fromChat(chat)

                val request = CallbackRequest.fromCallbackUser(
                    callbackQuery.data, userDto, bot, ChatId.fromId(chat.id), messageId,
                )

                Layout.layoutHeader(request)
                routeCallback(request)
            }
        }
    }.startPolling()
}
