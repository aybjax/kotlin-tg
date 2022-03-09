package mechanicum

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import mechanicum.db.models.CourseEntity
import network.request.RequestPage
import network.request.TgRequest
import org.jetbrains.exposed.sql.transactions.transaction

fun listCourses(request: TgRequest) {
    var page = RequestPage(1L);
    var pageCount: Long = 0L;

    val courses = transaction {
        page = RequestPage.fromQuery(request.getQuery("page"));

        val query = CourseEntity.all()
        pageCount = page.getTotalPageCount(query.count())

        if(page.value > pageCount) page = RequestPage(pageCount)

        query.limit(page.sqlLimit, page.sqlOffset).toList();
    }

    val min_id = courses.firstOrNull()?.id
    val max_id = courses.lastOrNull()?.id

    val coursesText = courses.joinToString("\n") {
        val description = it.description

        "${it.id}. *${it.name}* ${ if(description.isNotEmpty())
            "(`" else " "}$description${ if(description.isNotEmpty()) "`)" else ""}"
    }

    val buttons = mutableListOf<InlineKeyboardButton>()

    if(! page.isFirstPage()) {
        buttons.add(
            InlineKeyboardButton.CallbackData(text = "Предыдущий",
                callbackData = "mechanicum-courses?page=${page.prev}")
        )
    }

    buttons.add(
        InlineKeyboardButton.CallbackData(text = "Выбрать курс",
            callbackData = "choose-mechanicum-course-id"),
    )

    if(! page.isNextLastPage(pageCount)) {
        buttons.add(
            InlineKeyboardButton.CallbackData(text = "Следующий",
                callbackData = "mechanicum-courses?page=${page.next}")
        )
    }

    val inlineKeyboardMarkup = InlineKeyboardMarkup.create(buttons)

    request.bot.sendMessage(request.chatid,
        text = coursesText,
        parseMode = ParseMode.MARKDOWN,
        replyMarkup = inlineKeyboardMarkup,
    )

    val jumpButtons = mutableListOf<InlineKeyboardButton>()

    if(! page.isNextLastPage(pageCount)) {
        jumpButtons.add(
            InlineKeyboardButton.CallbackData(
                text = "вперед",
                callbackData = "forward-mechanicum-courses")
        )
    }

    if(! page.isFirstPage()) {
        jumpButtons.add(
            InlineKeyboardButton.CallbackData(
                text = "назад",
                callbackData = "backwards-mechanicum-courses")
        )
    }

    request.bot.sendMessage(request.chatid,
        text = "Перепрыгнуть страницы",
        replyMarkup = InlineKeyboardMarkup.create(
            jumpButtons
        ),
    )

    val userConfigurations = request.user.configurations
    userConfigurations?.previous_query = "mechanicum-courses"
    userConfigurations?.prev_page = page.value
    userConfigurations?.course_min = min_id?.value
    userConfigurations?.course_max = max_id?.value

    transaction {
        request.user.configurations = userConfigurations
    }
}