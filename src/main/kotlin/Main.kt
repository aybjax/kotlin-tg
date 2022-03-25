import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.logging.LogLevel
import constants.EnvVars
import db.initDatabase
import db.migrateDatabase
import db.models.User
import db.seedDatabase
import kotlinx.coroutines.runBlocking
import network.req_resp.CallbackRequest
import network.req_resp.TextRequest
import network.route.Layout
import network.route.routeCallback

fun main(args: Array<String>) = runBlocking {
    initDatabase()

    if(args.isNotEmpty()) {
        migrateDatabase()
        seedDatabase()
    }

    bot {
        token = EnvVars.MECHANICUM_TELEGRAM_TOKEN
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
