package controllers_products

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import dataclasses.Anchor
import dataclasses.RequestPage
import dataclasses.RouteQueryPair
import dataclasses.queries
import dataclasses.request.CallbackRequest
import dataclasses.request.TextRequest
import db.models.*
import extensions.plusOne
import extensions.roundDecimal
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import routes.CommonRouter
import routes.enums.EmptyRoutes
import routes.enums.MechanicumRoutes
import routes.enums.RoqedRoutes
import routes.enums.Routes

object RoqedController {
    /**
     * List mechanicum courses: search by name and paginate
     */
    fun listCourses(request: CallbackRequest): Boolean {
        val (courses, page, pageCount) = CourseRoqedDao.getCoursePageCount(
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
                Anchor(text = "⬅", RoqedRoutes.ROQED_COURSES queries mapOf("page" to page.prev.toString()))
            )
        }
        anchors.add(
            Anchor(text = "Выбрать курс \uD83C\uDD97", RouteQueryPair(RoqedRoutes.CHOOSE_ROQED_COURSE_ID))
        )
        if(page notLastPageFor pageCount) {
            anchors.add(
                Anchor(text = "➡", RoqedRoutes.ROQED_COURSES queries mapOf("page" to page.next.toString()))
            )
        }

        if(page.isFirstPage()) {
            val text = """
                Выберите номер курса в списке ниже.
                Для перелистывания на предыдущую или следующую страницу нажмите соответствующие кнопки.
                Для перелистывания на несколько страниц вперед ил назад нажмите соответствующие кнопки.
                Список курсов:
            """.trimIndent()
            request.writeButton(text)
        }

        val jumpButtons = mutableListOf<Anchor>()

        if(page.isNotFirstPage()) {
            jumpButtons.add(
                Anchor(text = "⏪⏪", RouteQueryPair(RoqedRoutes.BACKWARDS_ROQED_COURSES))
            )
        }

        if(page notLastPageFor pageCount) {
            jumpButtons.add(
                Anchor(text = "⏩⏩", RouteQueryPair(RoqedRoutes.FORWARD_ROQED_COURSES))
            )
        }

        if(courses.isEmpty()) {
            request.writeLink("*Ничего не найдено*", jumpButtons)
        }

        val buttons = mutableListOf(
            Anchor(text = "\uD83D\uDD0D Поиск по названию", RouteQueryPair(RoqedRoutes.ROQED_SEARCH_NAME))
        )

        if(! request.user.configurations?.searchName.isNullOrEmpty()) {
            buttons.add(Anchor(text = "❌ Отменить поиск", RouteQueryPair(RoqedRoutes.ROQED_SEARCH_NAME_CANCEL)))
        }

        request.writeLink(coursesText, listOf(
            anchors,
            jumpButtons,
            buttons,
        ))

        request.user.updateConfiguration {
            it.prev_page = page.value
            it.course_ids = ids

            it
        }

        return true;
    }

    fun searchName(request: CallbackRequest): Boolean {
        request.writeButton("_Поиск по названию курса в Roqed:_")

        return true
    }

    fun forwardCourses(request: CallbackRequest): Boolean {
        request.writeLink("_Cтраниц для перелистывания:_", rewindButtons)

        return true
    }

    fun forwardInput(request: CallbackRequest): Boolean {
        val digit = request.queries.get("digit")
        var currentDigit: String = ""

        return digit?.let {
            request.user.updateConfiguration { configurations ->
                val prev_digit = configurations.previous_input ?: ""
                val isCurrent = configurations.previous_input_route == RoqedRoutes.FORWARD_ROQED_INPUT.toString()

                if(digit == "10" && isCurrent) {
                    currentDigit = prev_digit.dropLast(1)
                }
                else if(prev_digit.isNotEmpty() && isCurrent) {
                    currentDigit = prev_digit + digit
                }
                else {
                    currentDigit = digit
                }

                configurations.previous_input = currentDigit
                configurations.previous_input_route = request.route.toString()

                configurations
            }

            request.writeLink("_Cтраниц для перелистывания:_ $currentDigit", rewindButtons, true)

            true
        } ?:
        run {
            val prev_digit = request.user.configurations?.previous_input

            prev_digit?.let {
                TextRequest(
                    user = request.user,
                    text = it,
                    bot = request.bot,
                    chatId = request.chatId,
                    messageId = request.messageId,
                ).toCallbackRequest()?.let {
                    CommonRouter.routeCallback(it)
                }
            }

            false
        }
    }

    fun backwordsCourses(request: CallbackRequest): Boolean {
        request.writeLink("_Cтраниц для перелистывания:_", rewindButtons)

        return true
    }

    fun backwordsInput(request: CallbackRequest): Boolean {
        val digit = request.queries.get("digit")
        var currentDigit: String = ""

        return digit?.let {
            request.user.updateConfiguration { configurations ->
                val prev_digit = configurations.previous_input ?: ""
                val isCurrent = configurations.previous_input_route == RoqedRoutes.BACKWARDS_ROQED_INPUT.toString()

                if(digit == "10" && isCurrent) {
                    currentDigit = prev_digit.dropLast(1)
                }
                else if(prev_digit.isNotEmpty() && isCurrent) {
                    currentDigit = prev_digit + digit
                }
                else {
                    currentDigit = digit
                }

                configurations.previous_input = currentDigit
                configurations.previous_input_route = request.route.toString()

                configurations
            }

            request.writeLink("_Cтраниц для перелистывания:_ $currentDigit", rewindButtons, true)

            true
        } ?:
        run {
            val prev_digit = request.user.configurations?.previous_input

            prev_digit?.let {
                TextRequest(
                    user = request.user,
                    text = it,
                    bot = request.bot,
                    chatId = request.chatId,
                    messageId = request.messageId,
                ).toCallbackRequest()?.let {
                    CommonRouter.routeCallback(it)
                }
            }

            false
        }
    }

    fun chooseCourse(request: CallbackRequest): Boolean {
        val buttons = request.user.configurations?.course_ids?.map { listOf(it.toString()) } ?: emptyList()
        val finalButtons = buttons + listOf(listOf("\uD83C\uDFE0 Домой"))

        request.writeButtons("_Введите номер курса_:", finalButtons)

        return true
    }

    fun courseChosen(request: CallbackRequest): Boolean {
        val course = transaction {
            CourseRoqedDao.findById(request.getQuery<Int>("course_id"))
        }

        request.writeButton("*Курс выбран:*")

        val msg = """
                    _Название курса:_ *${course?.name}*
                    _Количество процессов:_ *${course?.processesCount}*
                """.trimIndent()

        val button = listOf(
            Anchor("Начать курс" , RouteQueryPair(RoqedRoutes.START_ROQED_COURSE)),
        )

        val total = course?.processesCount ?: -1

        if(total > 0) {
            request.writeLink(msg, button)
        }
        else {
            val msg = """
                    _Название курса:_ *${course?.name}*
                    *К сожалению, курс пуст*
                """.trimIndent()

            request.writeButton(msg)
        }

        return true
    }

    fun startCourse(request: CallbackRequest): Boolean {
        val nextOrder = request.user.configurations?.next_process_order ?: return false
        val processCount = request.user.configurations?.total_processes ?: return false
        val courseId = request.user.configurations?.course_id ?: return false

        val process = transaction {
            ProcessRoqedDao.find {
                (Processes_Roqed.course eq courseId).and {
                    (Processes_Roqed.order eq nextOrder)
                }
            }.
            firstOrNull()
        }

        request.user.updateConfiguration {
            it.next_process_order = it.next_process_order?.plusOne()

            it
        }

        request.getQueryOrNull<String>("action")?.let { action ->
            if(action == "done") {
                request.user.updateConfiguration {
                    it.correct_processes = it.correct_processes?.plusOne()

                    it
                }
            }
        }

        if(nextOrder > processCount) {
            transaction {
                CourseRoqedDao.findById(courseId)
            } ?: return false

            val configurations = request.user.configurations
            val correct = configurations?.correct_processes ?: 0
            val total = configurations?.total_processes ?: -1

            request.writeButton("*Курс пройден*: $correct из $total правильных")

            if(total > 0) {
                request.writeButton("${(correct.toDouble()/total.toDouble() * 100).roundDecimal()}")
            }

            request.user.updateConfiguration {
                it.course_id = null
                it.next_process_order = null
                it.total_processes = -1
                it.correct_processes = 0

                it
            }
        }
        else {
            val msg = """
                            $nextOrder.
                            *${process?.description?.trim()}*:
                            ${process?.detailing?.trim()}
                        """.trimIndent()

            val buttons = listOf(
                Anchor("Сделано", RoqedRoutes.START_ROQED_COURSE queries mapOf("action" to "done")),
                Anchor("Пропустить", RouteQueryPair(RoqedRoutes.START_ROQED_COURSE))
            )

            request.writeLink(msg, buttons)
        }

        return true
    }

    fun cancelSearch(request: CallbackRequest): Boolean {
        request.user.updateConfiguration {
            it.searchName = null

            it
        }

        request.updateRouteQuery(RoqedRoutes.ROQED_COURSES)?.let {
            CommonRouter.routeCallback(it)
        }

        return false
    }


    val rewindButtons: List<List<Anchor>> by lazy {
        listOf(
            listOf(
                Anchor("1", RoqedRoutes.FORWARD_ROQED_INPUT queries mapOf("digit" to "1")),
                Anchor("2", RoqedRoutes.FORWARD_ROQED_INPUT queries mapOf("digit" to "2")),
                Anchor("3", RoqedRoutes.FORWARD_ROQED_INPUT queries mapOf("digit" to "3")),
            ),
            listOf(
                Anchor("4", RoqedRoutes.FORWARD_ROQED_INPUT queries mapOf("digit" to "4")),
                Anchor("5", RoqedRoutes.FORWARD_ROQED_INPUT queries mapOf("digit" to "5")),
                Anchor("6", RoqedRoutes.FORWARD_ROQED_INPUT queries mapOf("digit" to "6")),
            ),
            listOf(
                Anchor("7", RoqedRoutes.FORWARD_ROQED_INPUT queries mapOf("digit" to "7")),
                Anchor("8", RoqedRoutes.FORWARD_ROQED_INPUT queries mapOf("digit" to "8")),
                Anchor("9", RoqedRoutes.FORWARD_ROQED_INPUT queries mapOf("digit" to "9")),
            ),
            listOf(
                Anchor("0", RoqedRoutes.FORWARD_ROQED_INPUT queries mapOf("digit" to "0")),
                Anchor("\uD83C\uDD97", RouteQueryPair(RoqedRoutes.FORWARD_ROQED_INPUT)),
                Anchor("⬅️", RoqedRoutes.FORWARD_ROQED_INPUT queries mapOf("digit" to "10")),
            ),
        )
    }

    fun getCallbackQuery(previousQuery: Routes, text: String, request: TextRequest): RouteQueryPair? {
        if(previousQuery == RoqedRoutes.ROQED_SEARCH_NAME) {
            request.user.updateConfiguration {
                it.searchName = text.split(' ').joinToString("%", prefix = "%", postfix = "%")

                it
            }

            return RoqedRoutes.ROQED_COURSES queries emptyMap()
        }

        if(previousQuery == RoqedRoutes.BACKWARDS_ROQED_COURSES ||
            previousQuery == RoqedRoutes.BACKWARDS_ROQED_INPUT) {
            val page = (request.user.configurations?.prev_page ?: 1) - text.toLong()

            return RoqedRoutes.ROQED_COURSES queries mapOf("page" to page.toString())
        }

        if(previousQuery == RoqedRoutes.FORWARD_ROQED_COURSES ||
            previousQuery == RoqedRoutes.FORWARD_ROQED_INPUT) {
            val page = (request.user.configurations?.prev_page ?: 1) + text.toLong()

            return RoqedRoutes.ROQED_COURSES queries mapOf("page" to page.toString())
        }

        if(previousQuery == RoqedRoutes.CHOOSE_ROQED_COURSE_ID ||
            previousQuery == RoqedRoutes.ROQED_COURSES) {
            val id = text.toInt()
            val ids = request.user.configurations?.course_ids ?: emptyList()

            if (! ids.contains(id)) {
                request.writeButton("Номер курса должны быть *${ids.joinToString(", ")}*")


                val buttons = request.user.configurations?.course_ids?.map { listOf(it.toString()) } ?: emptyList()
                val finalButtons = buttons + listOf(listOf("\uD83C\uDFE0 Домой"))

                request.writeButtons("_Введите номер курса_:", finalButtons)

                return EmptyRoutes queries emptyMap()
            }

            request.user.updateConfiguration {
                val course = CourseRoqedDao.findById(id)

                it.total_processes = course?.processesCount ?: 0
                it.course_id = id
                it.next_process_order = 1
                it.correct_processes = 0

                it
            }

            return RoqedRoutes.CHOSEN_ROQED_COURSE_ID queries mapOf("course_id" to id.toString())
        }

        return null
    }
}
