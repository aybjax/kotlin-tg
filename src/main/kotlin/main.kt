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
import routes.enums.CommonRoutes
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main(args: Array<String>) = runBlocking {
    EnvVars.checkArgs()

    DatabaseObject.dbConnect()

    val bot = bot {
        token = EnvVars.TELEGRAM_TOKEN
        timeout = 30
        logLevel = LogLevel.All()

        dispatch {
            text {
                val chat = message?.chat ?: return@text

                val userDto = User.About.fromChat(chat)

                TextRequest.fromTextUser(
                    text, userDto, bot, ChatId.fromId(chat.id), message.messageId
                ).toCallbackRequest()?.let { request ->
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


            location {
                val chat = message?.chat ?: return@location
                val messageId = message?.messageId ?: return@location

                val userDto = User.About.fromChat(chat)

                CallbackRequest.fromCallbackUser(
                    CommonRoutes.LOCATION.toString(), userDto, bot, ChatId.fromId(chat.id), messageId,
                )?.let { request ->
                    request.user.updateCompletion {
                        it.latitude = location.latitude
                        it.longitude = location.longitude

                        it
                    }

                    request.writeButton("Ваше локация заптсана")
                }
            }
        }
    }

    if(args.isNotEmpty()) {
        println("Arguments present: starting migration")

        bot.sendMessage(
            ChatId.fromId(EnvVars.AYBJAXDIMEDUS),
            "Starting migrations...",
        )

        when(args[0]) {
            "migrate" -> try {
                DatabaseObject.migrateDatabase()
                DatabaseObject.seedDatabase()
            }
            catch (ex: Exception) {
                bot.sendMessage(
                    ChatId.fromId(EnvVars.AYBJAXDIMEDUS),
                    "Error on migration: ${ex.message}",
                )
            }

            "user" -> try {
                DatabaseObject.migrateUser()
            }
            catch (ex: Exception) {
                bot.sendMessage(
                    ChatId.fromId(EnvVars.AYBJAXDIMEDUS),
                    "Error on migration: ${ex.message}",
                )
            }
        }

        bot.sendMessage(
            ChatId.fromId(EnvVars.AYBJAXDIMEDUS),
            "Ended migration",
        )

        println("Completed migration")

        return@runBlocking
    }
    else {
        println("Arguments absent")
    }

    val (userResp, _) = bot.getMe()
    val botName = userResp?.body()?.result?.username ?: "UNKNOWN"

    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formatted = current.format(formatter)

    bot.sendMessage(
        ChatId.fromId(EnvVars.AYBJAXDIMEDUS),
        "$botName started at $formatted",
    )

    bot.startPolling()
}
