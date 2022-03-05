import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.logging.LogLevel
import constants.TELEGRAM_TOKEN
import db.initDatabase
import db.models.User
import examples.telegramsamples.generateUsersButton
import examples.telegramsamples.runDispatcherExample
import mechanicum.db.models.CourseEntity
import mechanicum.db.models.Courses
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

            callbackQuery() {
                val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
                val userId = callbackQuery.message?.from?.id ?: return@callbackQuery

                val request = TgRequest.fromCallbackUser(callbackQuery.data, userId, bot, ChatId.fromId(chatId));

                routeCallback(request)
            }
        }
    }.startPolling()
}

fun routeCallback(request: TgRequest) {
    when(request.route) {
        "mechanicum-courses" -> {
            var nextPage: Long = 0;
            val courses = transaction {
                val page: Long = request.getQuery("page")?.toLong() ?: 0;
                nextPage = page + 1

                CourseEntity.all().limit(5, 5 * page).toList();
            }

            val coursesButton = courses.map {
                InlineKeyboardButton.CallbackData(text = it.name,
                        callbackData = "mechanicum-processes?course=${it.id}")
            }

            val fullCoursesButton = coursesButton + InlineKeyboardButton.CallbackData(text = "Следующий",
                    callbackData = "mechanicum-processes?page=$nextPage")

            val inlineKeyboardMarkup = InlineKeyboardMarkup.create(fullCoursesButton)

            request.bot.sendMessage(request.chatid, text = "hello there")

            request.bot.sendMessage(request.chatid, text = "Hello", replyMarkup = inlineKeyboardMarkup)
        }
    }
}
