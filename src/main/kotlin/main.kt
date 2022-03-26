import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.logging.LogLevel
import containers.EnvVars
import db.DatabaseObject
import db.models.User
import kotlinx.coroutines.runBlocking
import dataclasses.request.CallbackRequest
import dataclasses.request.TextRequest
import routes.CommonRouter
import routes.Layout

fun main(args: Array<String>) = runBlocking {
    DatabaseObject.initDatabase()

    if(args.isNotEmpty()) {
        DatabaseObject.migrateDatabase()
        DatabaseObject.seedDatabase()

        println("Completed migration")

        return@runBlocking
    }

    bot {
        token = EnvVars.MECHANICUM_TELEGRAM_TOKEN
        timeout = 30
        logLevel = LogLevel.All()

        dispatch {
            text {
                val chat = message?.chat ?: return@text

                val userDto = User.About.fromChat(chat)

                val a = TextRequest.fromTextUser(
                    text, userDto, bot, ChatId.fromId(chat.id), message.messageId
                )

                val b = a.toCallbackRequest()

                b?.let { request ->
                    CommonRouter.routeCallback(request)
                }
            }

            callbackQuery() {
                val chat = callbackQuery.message?.chat ?: return@callbackQuery
                val messageId = callbackQuery.message?.messageId ?: return@callbackQuery

                val userDto = User.About.fromChat(chat)

                CallbackRequest.fromCallbackUser(
                    callbackQuery.data, userDto, bot, ChatId.fromId(chat.id), messageId,
                )?.let { request ->
                    Layout.layoutHeader(request)
                    CommonRouter.routeCallback(request)
                }
            }
        }
    }.startPolling()
}
