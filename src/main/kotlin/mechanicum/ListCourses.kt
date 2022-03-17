package mechanicum

import mechanicum.db.models.CourseDao
import network.req_resp.Anchor
import network.req_resp.CallbackRequest
import network.req_resp.RequestPage

/**
 * List mechanicum courses: search by name and paginate
 */
fun listCourses(request: CallbackRequest): Boolean {
    val (courses, page, pageCount) = CourseDao.getCoursePageCount(
        request.user.configurations?.searchName,
        RequestPage.fromString(request.getQueryOrNull("page"))
    )

    val ids = courses.map { it.id.value }

    val coursesText = courses.joinToString("\n") {
        val description = it.description

        "${it.id}. *${it.name}* ${ if(description.isNotEmpty())
            "(`" else " "}$description${ if(description.isNotEmpty()) "`)" else ""}\n\n"
    }

    val anchors = mutableListOf<Anchor>()
    if(page.isNotFirstPage()) {
        anchors.add(
            Anchor(text = "Предыдущий", link = "mechanicum-courses?page=${page.prev}")
        )
    }
    anchors.add(
        Anchor(text = "Выбрать курс", link = "choose-mechanicum-course-id")
    )
    if(page notLastPageFor pageCount) {
        anchors.add(
            Anchor(text = "Следующий", link = "mechanicum-courses?page=${page.next}")
        )
    }

    if(page.isFirstPage()) {
        val text = """
                Выберите номер курса в списке ниже.
                Для перелистывания на предыдущую или следующую страницу нажмите соответствующие кнопки.
                Для перелистывания на несколько страниц вперед ил назад нажмите соответствующие кнопки.
                Список курсов:
            """.trimIndent()
        request.writeText(text)
    }

    request.writeLink(coursesText, anchors)

    val jumpButtons = mutableListOf<Anchor>()

    if(page notLastPageFor pageCount) {
        jumpButtons.add(
            Anchor(text = "вперед", link = "forward-mechanicum-courses")
        )
    }

    if(page.isNotFirstPage()) {
        jumpButtons.add(
            Anchor(text = "назад", link = "backwards-mechanicum-courses")
        )
    }

    if(courses.isNotEmpty()) {
        request.writeLink("Перелистать страницы:", jumpButtons)
    }
    else {
        request.writeLink("*Ничего не найдено*", jumpButtons)
    }

    val buttons = mutableListOf<Anchor>(Anchor(text = "Ввести название", link = "mechanicum-search-name"))

    if(! request.user.configurations?.searchName.isNullOrEmpty()) {
        buttons.add(Anchor(text = "Отменить поиск", link = "mechanicum-search-name-cancel"))
    }

    request.writeLink("Поиск по названию:", buttons)

    request.user.updateConfiguration {
        it.prev_page = page.value
        it.course_ids = ids

        it
    }

    return true;
}