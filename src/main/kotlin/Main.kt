import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.logging.LogLevel
import constants.MECHANICUM_TELEGRAM_TOKEN
import db.initDatabase
import network.req_resp.CallbackRequest
import network.req_resp.TextRequest
import network.route.layoutHeader
import network.route.routeCallback

fun main() {
    initDatabase(true)

    bot {
        token = MECHANICUM_TELEGRAM_TOKEN
        timeout = 30
        logLevel = LogLevel.Network.Body

        dispatch {
            text {
                val chatId = message?.chat?.id ?: return@text

                val request = TextRequest.fromTextUser(text, chatId, bot, ChatId.fromId(chatId)).toCallbackRequest()

                routeCallback(request)
            }

            callbackQuery() {
                val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery

                val request = CallbackRequest.fromCallbackUser(
                    callbackQuery.data, chatId, bot, ChatId.fromId(chatId)
                )

                layoutHeader(request)
                routeCallback(request)
            }
        }
    }.startPolling()
}
