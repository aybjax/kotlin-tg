package mechanicum

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import mechanicum.db.models.CourseEntity
import network.request.RequestPage
import network.request.TgRequest
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.properties.Delegates

fun listCourses(request: TgRequest) {
    var page = RequestPage();
    var pageCount by Delegates.notNull<Long>()

    val courses = transaction {
        page = RequestPage.fromQuery(request.getQueryOrNull("page"));

        val query = CourseEntity.all()
        pageCount = page.getTotalPageCount(query.count())

        if(page.value > pageCount) page = --page

        query.limit(page.sqlLimit, page.sqlOffset).toList();
    }

    println(page.value)

    val minId = courses.firstOrNull()?.id
    val maxId = courses.lastOrNull()?.id

    val coursesText = courses.joinToString("\n") {
        val description = it.description

        "${it.id}. *${it.name}* ${ if(description.isNotEmpty())
            "(`" else " "}$description${ if(description.isNotEmpty()) "`)" else ""}\n\n"
    }

    val buttons = mutableListOf<InlineKeyboardButton>()

    if(page.isNotFirstPage()) {
        buttons.add(
            InlineKeyboardButton.CallbackData(text = "Предыдущий",
                callbackData = "mechanicum-courses?page=${page.prev}")
        )
    }

    buttons.add(
        InlineKeyboardButton.CallbackData(text = "Выбрать курс",
            callbackData = "choose-mechanicum-course-id"),
    )

    if(page notLastPageFor pageCount) {
        buttons.add(
            InlineKeyboardButton.CallbackData(text = "Следующий",
                callbackData = "mechanicum-courses?page=${page.next}")
        )
    }

    val inlineKeyboardMarkup = InlineKeyboardMarkup.create(buttons)

    if(page.isFirstPage()) {
        request.bot.sendMessage(request.chatid,
            text = buildString {
               append(
                   "`Выберите номер курса в списке ниже.\n"
               )
                append(
                    "Для перелистывания на предыдущую или следующую страницу нажмите соответствующие кнопки.\n"
                )
                append(
                    "Для перелистывания на несколько страниц вперед ил назад нажмите соответствующие кнопки.`\n"
                )
                append(
                    "Список курсов:"
                )
            },
            parseMode = ParseMode.MARKDOWN,
        )
    }

    request.bot.sendMessage(request.chatid,
        text = coursesText,
        parseMode = ParseMode.MARKDOWN,
        replyMarkup = inlineKeyboardMarkup,
    )

    val jumpButtons = mutableListOf<InlineKeyboardButton>()

    if(page notLastPageFor pageCount) {
        jumpButtons.add(
            InlineKeyboardButton.CallbackData(
                text = "вперед",
                callbackData = "forward-mechanicum-courses")
        )
    }

    if(page.isNotFirstPage()) {
        jumpButtons.add(
            InlineKeyboardButton.CallbackData(
                text = "назад",
                callbackData = "backwards-mechanicum-courses")
        )
    }

    request.bot.sendMessage(request.chatid,
        text = """
            Перелистать страницы:
        """.trimIndent(),
        replyMarkup = InlineKeyboardMarkup.create(
            jumpButtons
        ),
    )

    val userConfigurations = request.user.configurations
    userConfigurations?.previous_query = "mechanicum-courses"
    userConfigurations?.prev_page = page.value
    userConfigurations?.course_min = minId?.value
    userConfigurations?.course_max = maxId?.value

    transaction {
        request.user.configurations = userConfigurations
    }
}