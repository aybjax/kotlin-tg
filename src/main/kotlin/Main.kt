import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import constants.TELEGRAM_TOKEN
import db.initDatabase
import mechanicum.listCourses
import network.request.RequestType
import network.request.TgRequest
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    initDatabase(true)

    bot {
        token = TELEGRAM_TOKEN
        timeout = 30

        dispatch {
            command("start") {
                val markdownV2Text = """
                    Вас приветствует ассистент телеграм бот VargatesBot
                    Для продолжения *выберите продукт*
                """.trimIndent()

                val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                    listOf(InlineKeyboardButton.CallbackData(text = "Выбрать Mechanicum",
                        callbackData = "mechanicum-courses")),
                )
                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = markdownV2Text,
                    parseMode = ParseMode.MARKDOWN_V2,
                    replyMarkup = inlineKeyboardMarkup,
                )
            }

            text {
                val chatId = message?.chat?.id ?: return@text
//                val userId = message?.from?.id ?: return@text // both are same

                val request = TgRequest.fromCallbackUser(RequestType.TEXT,
                    text.split(" ")[0], chatId, bot, ChatId.fromId(chatId));

                routeCallback(request)
            }

            callbackQuery() {
                val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
//                val userId = callbackQuery.message?.from?.id ?: return@callbackQuery

                val request = TgRequest.fromCallbackUser(RequestType.CALLBACK,
                    callbackQuery.data, chatId, bot, ChatId.fromId(chatId));

                routeCallback(request)
            }
        }
    }.startPolling()
}

fun routeCallback(request: TgRequest) {
    when(request.type) {
        RequestType.CALLBACK -> {
            when(request.route) {
                "mechanicum-courses" -> {
                    listCourses(request)
                }

                "forward-mechanicum-courses" -> {
                    request.bot.sendMessage(request.chatid, text="_Введите кол-во прыжка_:",
                        parseMode = ParseMode.MARKDOWN)

                    val userConfigurations = request.user.configurations
                    userConfigurations?.previous_query = "forward-mechanicum-courses"

                    transaction {
                        request.user.configurations = userConfigurations
                    }
                }

                "backwards-mechanicum-courses" -> {
                    request.bot.sendMessage(request.chatid, text="_Введите кол-во прыжка_:",
                        parseMode = ParseMode.MARKDOWN)

                    val userConfigurations = request.user.configurations
                    userConfigurations?.previous_query = "backwards-mechanicum-courses"

                    transaction {
                        request.user.configurations = userConfigurations
                    }
                }
            }
        }
        RequestType.TEXT -> {
            when(request.user.configurations?.previous_query) {
                "backwards-mechanicum-courses" -> {
                    val page = (request.user.configurations?.prev_page ?: 1) -
                            request.route.toLong()
                    request.updateRouteQuery(
                        "mechanicum-courses?page=$page",
                        RequestType.CALLBACK,
                    )
                }

                "forward-mechanicum-courses" -> {
                    val page = (request.user.configurations?.prev_page ?: 1) +
                            request.route.toLong()
                    request.updateRouteQuery(
                        "mechanicum-courses?page=$page",
                        RequestType.CALLBACK,
                    )
                }
            }

            routeCallback(request)
        }
    }
}
