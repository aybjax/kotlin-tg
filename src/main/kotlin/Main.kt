import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import constants.TELEGRAM_TOKEN
import db.initDatabase
import extensions.normalizedWord
import extensions.plusOne
import extensions.roundDecimal
import mechanicum.db.models.CourseEntity
import mechanicum.db.models.ProcessEntity
import mechanicum.db.models.Processes
import mechanicum.listCourses
import mechanicum.start
import network.request.RequestType
import network.request.TgRequest
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    initDatabase(true)

    bot {
        token = TELEGRAM_TOKEN
        timeout = 30

        dispatch {
            text {
                val chatId = message?.chat?.id ?: return@text
//                val userId = message?.from?.id ?: return@text // both are same

                var text = text.normalizedWord()

                if(text == "начать") {
                    text = "/start"
                }

                val request = TgRequest.fromCallbackUser(RequestType.TEXT,
                    text, chatId, bot, ChatId.fromId(chatId));

                if(text == "/start") {
                    start(request)

                    return@text
                }

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
                    request.bot.sendMessage(request.chatid, text="_Введите кол-во страниц для перелистывания_:",
                        parseMode = ParseMode.MARKDOWN)

                    val userConfigurations = request.user.configurations
                    userConfigurations?.previous_query = "forward-mechanicum-courses"

                    transaction {
                        request.user.configurations = userConfigurations
                    }
                }

                "backwards-mechanicum-courses" -> {
                    request.bot.sendMessage(request.chatid, text="_Введите кол-во страниц для перелистывания_:",
                        parseMode = ParseMode.MARKDOWN)

                    val userConfigurations = request.user.configurations
                    userConfigurations?.previous_query = "backwards-mechanicum-courses"

                    transaction {
                        request.user.configurations = userConfigurations
                    }
                }

                "choose-mechanicum-course-id" -> {
                    request.bot.sendMessage(request.chatid, text="_Введите номер курса_:",
                        parseMode = ParseMode.MARKDOWN)

                    val userConfigurations = request.user.configurations
                    userConfigurations?.previous_query = "choose-mechanicum-course-id"

                    transaction {
                        request.user.configurations = userConfigurations
                    }
                }

                "chosen-mechanicum-course-id" -> {
                    val course = transaction {
                        CourseEntity.findById(request.getQuery<Int>("course_id"))
                    }

                    val msg = """
                        _Номер курса:_ *${course?.id}*
                        _Название курса:_ *${course?.name}*
                        _Количество процессов:_ *${course?.processes_count}*
                    """.trimIndent()

                    val button = InlineKeyboardMarkup.create(listOf(
                        InlineKeyboardButton.CallbackData("Начать курс" ,"start-mechanicum-course"),
                    ))

                    request.bot.sendMessage(
                        request.chatid,
                        msg,
                        parseMode = ParseMode.MARKDOWN,
                        replyMarkup = button,
                    )
                }

                "start-mechanicum-course" -> {
                    val nextOrder = request.user.configurations?.next_process_order ?: return
                    val processCount = request.user.configurations?.total_processes ?: return
                    val courseId = request.user.configurations?.course_id ?: return


                    val process = transaction {
                        ProcessEntity.find {
                            (Processes.course eq courseId).and {
                                (Processes.order eq nextOrder)
                            }
                        }.
                            firstOrNull()
                    }


                    val configurations = request.user.configurations
                    configurations?.next_process_order = configurations?.next_process_order?.plusOne()

                    request.getQueryOrNull<String>("action")?.let {
                        if(it == "done") {
                            configurations?.correct_processes = configurations?.correct_processes?.plusOne()
                        }
                    }

                    transaction {
                        request.user.configurations = configurations
                    }

                    if(nextOrder > processCount) {
                        val course = transaction {
                            CourseEntity.findById(courseId)
                        } ?: return

                        request.bot.sendMessage(
                            request.chatid,
                            """
                                Вы закончили курс *${course.name}*
                            """.trimIndent(),
                            parseMode = ParseMode.MARKDOWN,
                        )

                        val configurations = request.user.configurations
                        val correct = configurations?.correct_processes ?: 0
                        val total = configurations?.total_processes ?: -1


                        request.bot.sendMessage(
                            request.chatid,
                            """
                                Результат: $correct из $total (${(correct.toDouble()/total.toDouble() * 100).
                                                                                                    roundDecimal()}%)
                            """.trimIndent(),
                            parseMode = ParseMode.MARKDOWN,
                        )

                        configurations?.course_id = null
                        configurations?.next_process_order = null
                        configurations?.total_processes = -1
                        configurations?.correct_processes = 0

                        transaction {
                            request.user.configurations = configurations
                        }

                        return
                    }

                    val msg = """
                        $nextOrder.
                        *${process?.description?.trim()}*
                        ${process?.detailing?.trim()}
                    """.trimIndent()

                    val buttons = InlineKeyboardMarkup.create(listOf(
                        InlineKeyboardButton.CallbackData("Сделано", "start-mechanicum-course?action=done"),
                        InlineKeyboardButton.CallbackData("Пропустить", "start-mechanicum-course")
                    ))

                    request.bot.sendMessage(
                        request.chatid,
                        msg,
                        parseMode = ParseMode.MARKDOWN,
                        replyMarkup = buttons,
                    )
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

                "choose-mechanicum-course-id" -> {
                    val id = request.route.toInt()
                    val min = request.user.configurations?.course_min ?: return;
                    val max = request.user.configurations?.course_max ?: return;

                    if (
                        id < min || id > max
                    ) {
                        request.bot.sendMessage(
                            chatId = request.chatid,
                            text = "Номер курса должен быть между $min и $max, включительно"
                        )

                        return
                    }

                    request.updateRouteQuery("chosen-mechanicum-course-id?course_id=$id", RequestType.CALLBACK)
                    val configurations = request.user.configurations
                    configurations?.course_id = id;
                    configurations?.next_process_order = 1;
                    configurations?.correct_processes = 0;

                    transaction {
                        val course = CourseEntity.findById(id)
                        configurations?.total_processes = course?.processes_count ?: 0
                        request.user.configurations = configurations
                    }
                }
            }

            if(request.type == RequestType.CALLBACK) routeCallback(request)
        }
    }
}
