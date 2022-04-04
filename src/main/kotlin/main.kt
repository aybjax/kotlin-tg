import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.logging.LogLevel
import controllers.GeoController
import dataclasses.geocoding.Latlong
import variables.DatabaseTelegramEnvVars
import db.DatabaseObject
import db.models.User
import kotlinx.coroutines.runBlocking
import dataclasses.request.CallbackRequest
import dataclasses.request.Request
import dataclasses.request.TextRequest
import routes.CommonRouter
import routes.Layout
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread
import kotlin.properties.Delegates


fun main(args: Array<String>) = runBlocking {
    DatabaseTelegramEnvVars.checkArgs()

    DatabaseObject.dbConnect()

    val bot = bot {
        token = DatabaseTelegramEnvVars.TELEGRAM_TOKEN
        timeout = 30
        logLevel = LogLevel.All()

        dispatch {
            text {
                val chat = message?.chat ?: return@text

                val userDto = User.About.fromChat(chat)
                var type by Delegates.notNull<Request.RequestType>()

                if(text.startsWith('/')) {
                    type = Request.RequestType.COMMAND
                }
                else {
                    type = Request.RequestType.TEXT
                }

                TextRequest.fromTextUser(
                    text, userDto, bot, ChatId.fromId(chat.id), message.messageId, type,
                ).toCallbackRequest()?.let { request ->
                    CommonRouter.routeCallback(request)
                }
            }

            callbackQuery() {
                val chat = callbackQuery.message?.chat ?: return@callbackQuery
                val messageId = callbackQuery.message?.messageId ?: return@callbackQuery

                val userDto = User.About.fromChat(chat)

                CallbackRequest.fromCallbackUser(
                    callbackQuery.data, userDto, bot, ChatId.fromId(chat.id), messageId, Request.RequestType.CALLBACK
                )?.let { request ->
                    Layout.layoutHeader(request)
                    CommonRouter.routeCallback(request)
                }
            }


            location {
                val chat = message?.chat ?: return@location
                val messageId = message?.messageId ?: return@location

                val userDto = User.About.fromChat(chat)


                TextRequest.fromTextUser(
                    "да", userDto, bot, ChatId.fromId(chat.id), messageId, Request.RequestType.TEXT,
                ).toCallbackRequest()?.let { request ->
                    thread(start = true) {
                        runBlocking {
                            val latlong = Latlong(
                                latitude = location.latitude.toDouble(),
                                longitude = location.longitude.toDouble(),
                            )
                            val location = GeoController.getLocation(request, latlong)

                            if(location.isNotEmpty()) {
                                request.user.updateCompletion {
                                    it.location = location
                                    it.latlong = latlong

                                    it
                                }
                            }
                        }
                    }

                    request.writeButton("Ваша локация записана")

                    CommonRouter.routeCallback(request)
                }
            }
        }
    }

    if(args.isNotEmpty()) {
        println("Arguments present: starting migration")

        bot.sendMessage(
            ChatId.fromId(DatabaseTelegramEnvVars.AYBJAXDIMEDUS),
            "Starting migrations...",
        )

        when(args[0]) {
            "migrate" -> try {
                DatabaseObject.migrateDatabase()
                DatabaseObject.seedDatabase()
            }
            catch (ex: Exception) {
                bot.sendMessage(
                    ChatId.fromId(DatabaseTelegramEnvVars.AYBJAXDIMEDUS),
                    "Error on migration: ${ex.message}",
                )
            }

            "user" -> try {
                DatabaseObject.migrateUser()
            }
            catch (ex: Exception) {
                bot.sendMessage(
                    ChatId.fromId(DatabaseTelegramEnvVars.AYBJAXDIMEDUS),
                    "Error on migration: ${ex.message}",
                )
            }

            "geodata" -> try {
                DatabaseObject.migrateGeoData()
            }
            catch(ex: Exception) {
                bot.sendMessage(
                    ChatId.fromId(DatabaseTelegramEnvVars.AYBJAXDIMEDUS),
                    "Error on migration: ${ex.message}",
                )
            }

            else -> bot.sendMessage(
                ChatId.fromId(DatabaseTelegramEnvVars.AYBJAXDIMEDUS),
                "No migration...",
            )
        }

        bot.sendMessage(
            ChatId.fromId(DatabaseTelegramEnvVars.AYBJAXDIMEDUS),
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
        ChatId.fromId(DatabaseTelegramEnvVars.AYBJAXDIMEDUS),
        "$botName started at $formatted",
    )

    bot.startPolling()
}