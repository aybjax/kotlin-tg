package controllers_products

import com.github.kotlintelegrambot.entities.TelegramFile
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import dataclasses.Anchor
import dataclasses.RequestPage
import dataclasses.RouteQueryPair
import dataclasses.queries
import dataclasses.request.CallbackRequest
import dataclasses.request.Request
import dataclasses.request.TextRequest
import db.models.CourseMechanicumDao
import db.models.ProcessMechanicumDao
import db.models.Processes_Mechanicum
import db.models.User
import extensions.plusOne
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import routes.CommonRouter
import routes.enums.MechanicumRoutes
import routes.enums.Routes
import java.io.File
import java.io.FileOutputStream
import java.util.stream.Stream


object MechanicumController {
    /**
     * List mechanicum courses: search by name and paginate
     */
    fun listCourses(request: CallbackRequest): Boolean {
        val (courses, page, pageCount) = CourseMechanicumDao.getCoursePageCount(
            request.user.routing?.searchName,
            RequestPage.fromString(request.getQueryOrNull("page"))
        )

        val ids = courses.map { it.id.value }

        if(page.isFirstPage()) {
            val text = """
                Выберите номер курса в списке ниже.
                Для перелистывания на предыдущую или следующую страницу нажмите соответствующие кнопки.
                Для перелистывания на несколько страниц вперед ил назад нажмите соответствующие кнопки.
                Список курсов:
            """.trimIndent()
            request.writeButton(text)
        }

        courses.dropLast(1).forEach {
            val description = it.description

            val msg = "${it.id}. <b>${it.name}</b> ${ if(description.isNotEmpty())
                "(<pre>" else " "}$description${ if(description.isNotEmpty()) "</pre>)" else ""}\n\n"

            request.writeLink(
                msg,
                listOf(
                    Anchor(
                        "Выбрать",
                        MechanicumRoutes.BEFORE_CHOOSEN_MECHANICUM_COURSE_ID queries mapOf("course_id" to it.id.toString())
                    )
                ),
            )
        }

        val msg = courses.last().let {
            val description = it.description

            "${it.id}. <b>${it.name}</b> ${ if(description.isNotEmpty())
                "(<pre>" else " "}$description${ if(description.isNotEmpty()) "</pre>)" else ""}\n\n"
        }

        val chooseBtn = listOf(
            Anchor(
                "Выбрать",
                MechanicumRoutes.BEFORE_CHOOSEN_MECHANICUM_COURSE_ID queries mapOf("course_id" to courses.last().id.toString())
            )
        )

        val anchors = mutableListOf<Anchor>()

        if(page.isNotFirstPage()) {
            anchors.add(
                Anchor(text = "⬅", MechanicumRoutes.MECHANICUM_COURSES queries mapOf("page" to page.prev.toString()))
            )
        }

        if(page notLastPageFor pageCount) {
            anchors.add(
                Anchor(text = "➡", MechanicumRoutes.MECHANICUM_COURSES queries mapOf("page" to page.next.toString()))
            )
        }

        val jumpButtons = mutableListOf<Anchor>()

        if(page.isNotFirstPage()) {
            jumpButtons.add(
                Anchor(text = "⏪⏪", RouteQueryPair(MechanicumRoutes.BACKWARDS_MECHANICUM_COURSES))
            )
        }

        if(page notLastPageFor pageCount) {
            jumpButtons.add(
                Anchor(text = "⏩⏩", RouteQueryPair(MechanicumRoutes.FORWARD_MECHANICUM_COURSES))
            )
        }

        if(courses.isEmpty()) {
            request.writeLink("<b>Ничего не найдено</b>", jumpButtons)
        }

        val buttons = mutableListOf(
            Anchor(text = "\uD83D\uDD0D Поиск по названию", RouteQueryPair(MechanicumRoutes.MECHANICUM_SEARCH_NAME))
        )

        if(! request.user.routing?.searchName.isNullOrEmpty()) {
            buttons.add(Anchor(text = "❌ Отменить поиск", RouteQueryPair(MechanicumRoutes.MECHANICUM_SEARCH_NAME_CANCEL)))
        }

        request.writeLink(msg, listOf(
            chooseBtn,
            anchors,
            jumpButtons,
            buttons,
        ))

        request.user.updateRouting {
            it.prev_page = page.value
            it.course_ids = ids

            it
        }

        return true;
    }

    fun searchName(request: CallbackRequest): Boolean {
        request.writeButton("<i>Поиск по названию курса в Mechanicum:</i>")
        request.user.updateRouting {
            it.expectedQuery = User.Routing.ExpectedQuery(
                route = MechanicumRoutes.MECHANICUM_COURSES.toString(),
                action = User.Routing.ExpectedQuery.Action.SEARCH_NAME,
                requestType = Request.RequestType.TEXT,
            )

            it
        }

        return true
    }

    fun forwardCourses(request: CallbackRequest): Boolean {
        request.writeLink("<i>Cтраниц для перелистывания:</i>", rewindButtons)

        request.user.updateRouting {
            it.expectedQuery = User.Routing.ExpectedQuery(
                route = MechanicumRoutes.MECHANICUM_COURSES.toString(),
                action = User.Routing.ExpectedQuery.Action.FORWARD_PAGING,
                requestType = Request.RequestType.TEXT,
            )

            it
        }

        return true
    }

    fun forwardInput(request: CallbackRequest): Boolean {
        val digit = request.queries.get("digit")
        var currentDigit: String = ""

        return digit?.let {
            request.user.updateRouting { configurations ->
                val prev_digit = configurations.previous_input ?: ""
                val isCurrent = configurations.previous_input_route == MechanicumRoutes.FORWARD_MECHANICUM_INPUT.toString()

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

            request.writeLink("<i>Cтраниц для перелистывания:</i> $currentDigit", rewindButtons, true)

            true
        } ?:
        run {
            val prev_digit = request.user.routing?.previous_input

            prev_digit?.let {
                TextRequest(
                    user = request.user,
                    text = it,
                    bot = request.bot,
                    chatId = request.chatId,
                    messageId = request.messageId,
                    type = Request.RequestType.TEXT,
                ).toCallbackRequest()?.let {
                    CommonRouter.routeCallback(it)
                }
            }

            false
        }
    }

    fun backwordsCourses(request: CallbackRequest): Boolean {
        request.writeLink("<i>Cтраниц для перелистывания:</i>", rewindButtons)

        request.user.updateRouting {
            it.expectedQuery = User.Routing.ExpectedQuery(
                route = MechanicumRoutes.MECHANICUM_COURSES.toString(),
                action = User.Routing.ExpectedQuery.Action.BACKWARDS_PAGING,
                requestType = Request.RequestType.TEXT,
            )

            it
        }

        return true
    }

    fun backwordsInput(request: CallbackRequest): Boolean {
        val digit = request.queries.get("digit")
        var currentDigit: String = ""

        return digit?.let {
            request.user.updateRouting { configurations ->
                val prev_digit = configurations.previous_input ?: ""
                val isCurrent = configurations.previous_input_route == MechanicumRoutes.BACKWARDS_MECHANICUM_INPUT.toString()

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

            request.writeLink("<i>Cтраниц для перелистывания:</i> $currentDigit", rewindButtons, true)

            true
        } ?:
        run {
            val prev_digit = request.user.routing?.previous_input

            prev_digit?.let {
                TextRequest(
                    user = request.user,
                    text = it,
                    bot = request.bot,
                    chatId = request.chatId,
                    messageId = request.messageId,
                    type = Request.RequestType.TEXT,
                ).toCallbackRequest()?.let {
                    CommonRouter.routeCallback(it)
                }
            }

            false
        }
    }

    fun chooseCourse(request: CallbackRequest): Boolean {
        val buttons = request.user.routing?.course_ids?.map { listOf(it.toString()) } ?: emptyList()
        val finalButtons = buttons + listOf(listOf("\uD83C\uDFE0 Домой"))

        request.writeButtons("<i>Введите номер курса</i>:", finalButtons)

        request.user.updateRouting {
            it.expectedQuery = User.Routing.ExpectedQuery(
                route = MechanicumRoutes.CHOSEN_MECHANICUM_COURSE_ID.toString(),
            )

            it
        }

        return true
    }

    fun getLocation(request: CallbackRequest) {
        request.writeButtons("""
            <b>Курс выбран:</b>
            Где Вы находитесь?
        """.trimIndent(),
            buttonTexts = listOf(listOf("Не отправлять")),
            locationText = "Отправить локацию", addHome = true)

        request.user.updateRouting {
            it.expectedQuery = User.Routing.ExpectedQuery(
                route = MechanicumRoutes.CHOSEN_MECHANICUM_COURSE_ID.toString(),
                requestType = Request.RequestType.TEXT,
                payload = request.getQuery<String>("course_id")
            )

            it
        }
    }

    fun courseChosen(request: CallbackRequest): Boolean {
        if(request.getQuery<Int>("answer") == 1) {
            request.writeButtons("""
                Спасибо 👍
            """.trimIndent())
        }
        else {
            request.writeButtons("""
                В следующий раз 😔
            """.trimIndent())
        }

        val course = transaction {
            CourseMechanicumDao.findById(request.getQuery<Int>("course_id"))
        }

        val msg = """
                            <i>Номер курса:</i> <b>${course?.id}</b>
                            <i>Название курса:</i> <b>${course?.name}</b>
                            <i>Количество процессов:</i> <b>${course?.processesCount}</b>
                        """.trimIndent()

        if ((course?.processesCount ?: 0) > 0) {
            val button = listOf(
                Anchor("Начать" , RouteQueryPair(MechanicumRoutes.START_MECHANICUM_COURSE)),
            )

            request.writeLink(msg, button)
        } else {
            request.writeButton(buildString {
                this.append(msg)
                this.append('\n')
                this.append("<b>К сожалению, курс пуст</b>")
            })
        }

        return true
    }

    fun startCourse(request: CallbackRequest): Boolean {
        val nextOrder = request.user.completion?.next_process_order ?: return false
        val currentOrder = nextOrder - 1
        val processCount = request.user.completion?.total_processes ?: return false
        val courseId = request.user.completion?.course_id ?: return false
        var endProcess = false

        val process = transaction {
            ProcessMechanicumDao.find {
                (Processes_Mechanicum.course eq courseId).and {
                    (Processes_Mechanicum.order eq nextOrder)
                }
            }.
            firstOrNull()
        } ?: run {
            request.writeButton("<b>Ошибка базы данных</b>")

            return true
        }

        request.getQueryOrNull<String>("action")?.let { action ->
            if(action == "done") {
                request.user.updateCompletion { completion ->
                    var alreadyWasThere = false
                    completion.correct_processes = completion.correct_processes?.plusOne()
                    val current = completion.processCompletions.lastOrNull() { processCompletion ->
                        alreadyWasThere = true
                        processCompletion.process_order == currentOrder
                    }?.let {
                        it.status = User.Completion.CompletionStatus.DONE
                        it
                    } ?: User.Completion.ProcessCompletion(
                        currentOrder,
                        process?.description ?: "",
                        User.Completion.CompletionStatus.DONE,
                        null,
                    )

                    if(alreadyWasThere) {
                        completion.processCompletions.removeLast()
                    }

                    completion.processCompletions.add(
                        current
                    )

                    completion
                }
            }
            else if(action == "fail") {
                request.user.updateCompletion { completion ->
                    val currentProcess = completion.processCompletions.firstOrNull {
                        currentOrder == it.process_order
                    }

                    if(currentProcess != null) {
                        currentProcess.status = User.Completion.CompletionStatus.PENDING_AFTER_FAIL

                        return@updateCompletion completion
                    }

                    completion.processCompletions.add(
                        User.Completion.ProcessCompletion(
                            currentOrder,
                            process?.description ?: "",
                            User.Completion.CompletionStatus.PENDING_AFTER_FAIL,
                            null,
                        )
                    )

                    completion
                }

                request.writeText("<i>Напишите причину:</i>")

                request.user.updateRouting {
                    it.expectedQuery = User.Routing.ExpectedQuery(
                        route = MechanicumRoutes.START_MECHANICUM_COURSE.toString(),
                        requestType = Request.RequestType.TEXT,
                    )

                    it
                }

                return true
            }
            else if(action == "comment") {
                request.writeText("<i>Напишите комментарий:</i>")

                request.user.updateRouting {
                    it.expectedQuery = User.Routing.ExpectedQuery(
                        route = MechanicumRoutes.START_MECHANICUM_COURSE.toString(),
                        requestType = Request.RequestType.TEXT,
                    )

                    it
                }

                return true
            }
            else if(action == "end") {
                endProcess = true
            }
            else if(action == "comment_added") {
                var removeLast = true;

                val currentCompletion = request.user.updateCompletion { it ->
                    val current = it.processCompletions.lastOrNull() { processCompletion ->
                        processCompletion.process_order == currentOrder
                    } ?: kotlin.run {
                        removeLast = false

                        User.Completion.ProcessCompletion(
                            currentOrder,
                            process?.description ?: "",
                            User.Completion.CompletionStatus.PENDING,
                            ""
                        )
                    }

                    current.comment = current.comment?.let { prev_comm ->
                        prev_comm + (if(prev_comm.isNotEmpty()) "\n" else "") + request.getQuery<String>("text");
                    } ?: request.getQuery<String>("text")

                    if(it.processCompletions.isNotEmpty() && removeLast) it.processCompletions.removeLast()
                    it.processCompletions.add(current)

                    it
                }

                if(currentCompletion?.processCompletions?.last()?.status == User.Completion.CompletionStatus.PENDING) {
                    return true
                }

                if(currentCompletion?.processCompletions?.last()?.status == User.Completion.CompletionStatus.PENDING_AFTER_FAIL) {
                    request.user.updateCompletion {
                        it.processCompletions?.last()?.status = User.Completion.CompletionStatus.FAIL

                        it
                    }
                }
            }

            return@let
        }

        request.user.updateCompletion {
            it.next_process_order = it.next_process_order?.plusOne()

            it
        }

        if(nextOrder > processCount || endProcess) {
            transaction {
                CourseMechanicumDao.findById(courseId)
            } ?: return false

            val completion = request.user.completion
            val correct = completion?.correct_processes ?: 0
            val total = completion?.total_processes ?: -1

            request.writeButton("<b>Работа завершена</b>: $correct из $total правильных")
            request.user.completion?.location?.let {
                request.writeButton("<b>Место прохождения</b>: $it")
            }
            request.user.completion?.processCompletions?.filter {
                it.status == User.Completion.CompletionStatus.FAIL
            }?.let { fails ->
                val failText = buildString {
                    if(fails.isNotEmpty()) {
                        this.append("<b>Пропущенные процессы:</b>\n\n")

                        fails.forEach {
                            if(! it.comment.isNullOrBlank()) {
                                this.append("${it.process_order}. <b>${it.process_name}</b>:\n")
                                this.append("\t\t\t<i>${it.comment?.split('\n')?.joinToString("\n\t\t") ?: ""}</i>\n")
                            }
                        }
                    }
                }

                request.writeButton(failText)
            }

            request.user.completion?.processCompletions?.filterNot {
                it.status == User.Completion.CompletionStatus.FAIL ||
                        it.comment.isNullOrBlank()
            }?.let { withComments ->
                val commentText = buildString {
                    if(withComments.isNotEmpty()) {
                        this.append("<b>Оставили комментарий:</b>\n\n")

                        withComments.forEach {
                            if(! it.comment.isNullOrBlank()) {
                                this.append("${it.process_order}. <b>${it.process_name}</b>:\n")
                                this.append("\t\t\t<i>${it.comment?.split('\n')?.joinToString("\n\t\t") ?: ""}</i>\n")
                            }
                        }
                    }
                }

                request.writeButton(commentText)
            }

            request.user.updateCompletion {
                it.course_id = null
                it.next_process_order = null
                it.total_processes = -1
                it.correct_processes = 0

                it
            }

            //
//            val document = Document()
//            PdfWriter.getInstance(document, FileOutputStream("aybjax.pdf"))
//
//            document.open()
//
//            val font = FontFactory.getFont(FontFactory.COURIER, 16F, BaseColor.BLACK)
//            val chunk = Chunk("Hello World", font)
//            document.add(chunk)
//            document.close()
//
//            request.bot.sendDocument(request.chatId,
//                TelegramFile.ByFile(File("aybjax.pdf")),
//                caption = "Ваши результаты")
            val document = Document()
            PdfWriter.getInstance(document, FileOutputStream("aybjax.pdf"))

            document.open()

            val font = FontFactory.getFont(FontFactory.COURIER, 16F, BaseColor.BLACK)
            val chunk = Chunk("Hello World", font)
            document.add(chunk)

            val table = PdfPTable(3)
//            addTableHeader(table)
            Stream.of("column header 1", "column header 2", "column header 3")
                .forEach { columnTitle: String? ->
                    val header = PdfPCell()
                    header.backgroundColor = BaseColor.LIGHT_GRAY
                    header.borderWidth = 2f
                    header.phrase = Phrase(columnTitle)
                    table.addCell(header)
                }

//            addRows(table)
            table.addCell("row 1, col 1");
            table.addCell("row 1, col 2");
            table.addCell("row 1, col 3");


//            addCustomRows(table)

            document.add(table)

            document.close()

            request.bot.sendDocument(request.chatId,
                TelegramFile.ByFile(File("aybjax.pdf")),
                caption = "Ваши результаты")
        }
        else {
            val msg = """
                            $nextOrder.
                            <b>${process?.description?.trim()}</b>:
                            ${process?.detailing?.trim()}
                        """.trimIndent()

            val buttons = listOf(
                listOf(
                    Anchor("Сделано", MechanicumRoutes.START_MECHANICUM_COURSE queries mapOf("action" to "done")),
                    Anchor("Пропустить", MechanicumRoutes.START_MECHANICUM_COURSE queries mapOf("action" to "fail")),
                ),
                listOf(
                    Anchor("Комментарий", MechanicumRoutes.START_MECHANICUM_COURSE queries mapOf("action" to "comment")),
                    Anchor("Закончить", MechanicumRoutes.START_MECHANICUM_COURSE queries mapOf("action" to "end")),
                )
            )

            request.writeLink(msg, buttons)
        }

        return true
    }

    fun cancelSearch(request: CallbackRequest): Boolean {
        request.user.updateRouting {
            it.searchName = null

            it
        }

        request.updateRouteQuery(MechanicumRoutes.MECHANICUM_COURSES)?.let {
            CommonRouter.routeCallback(it)
        }

        return false
    }


    val rewindButtons: List<List<Anchor>> by lazy {
        listOf(
            listOf(
                Anchor("1", MechanicumRoutes.FORWARD_MECHANICUM_INPUT queries mapOf("digit" to "1")),
                Anchor("2", MechanicumRoutes.FORWARD_MECHANICUM_INPUT queries mapOf("digit" to "2")),
                Anchor("3", MechanicumRoutes.FORWARD_MECHANICUM_INPUT queries mapOf("digit" to "3")),
            ),
            listOf(
                Anchor("4", MechanicumRoutes.FORWARD_MECHANICUM_INPUT queries mapOf("digit" to "4")),
                Anchor("5", MechanicumRoutes.FORWARD_MECHANICUM_INPUT queries mapOf("digit" to "5")),
                Anchor("6", MechanicumRoutes.FORWARD_MECHANICUM_INPUT queries mapOf("digit" to "6")),
            ),
            listOf(
                Anchor("7", MechanicumRoutes.FORWARD_MECHANICUM_INPUT queries mapOf("digit" to "7")),
                Anchor("8", MechanicumRoutes.FORWARD_MECHANICUM_INPUT queries mapOf("digit" to "8")),
                Anchor("9", MechanicumRoutes.FORWARD_MECHANICUM_INPUT queries mapOf("digit" to "9")),
            ),
            listOf(
                Anchor("0", MechanicumRoutes.FORWARD_MECHANICUM_INPUT queries mapOf("digit" to "0")),
                Anchor("\uD83C\uDD97", RouteQueryPair(MechanicumRoutes.FORWARD_MECHANICUM_INPUT)),
                Anchor("⬅️", MechanicumRoutes.FORWARD_MECHANICUM_INPUT queries mapOf("digit" to "10")),
            ),
        )
    }

    fun textToCallbackQuery(expectedRoute: Routes, text: String, request: TextRequest): RouteQueryPair? {
        val expectedQuery = request.user.routing!!.expectedQuery!!

        when(expectedQuery.requestType) {
            Request.RequestType.TEXT ->
                if(expectedRoute == MechanicumRoutes.MECHANICUM_COURSES) {
                    if(expectedQuery.action == User.Routing.ExpectedQuery.Action.SEARCH_NAME) {
                        request.user.updateRouting {
                            it.searchName = text.split(' ').joinToString("%", prefix = "%", postfix = "%")

                            it
                        }

                        return MechanicumRoutes.MECHANICUM_COURSES queries emptyMap()
                    }
                    else if (expectedQuery.action == User.Routing.ExpectedQuery.Action.BACKWARDS_PAGING) {
                        val page = (request.user.routing?.prev_page ?: 1) - text.toLong()

                        return MechanicumRoutes.MECHANICUM_COURSES queries mapOf("page" to page.toString())
                    }
                    else if(expectedQuery.action == User.Routing.ExpectedQuery.Action.FORWARD_PAGING) {
                        val page = (request.user.routing?.prev_page ?: 1) + text.toLong()

                        return MechanicumRoutes.MECHANICUM_COURSES queries mapOf("page" to page.toString())
                    }
                }
                else if(expectedRoute == MechanicumRoutes.CHOSEN_MECHANICUM_COURSE_ID) {
                    // FIXME
                    val id = (expectedQuery.payload as String).toInt()
//                    val ids = request.user.routing?.course_ids ?: emptyList()

//                    if (! ids.contains(id)) {
//                        request.writeButton("Номер курса должны быть <b>${ids.joinToString(", ")}</b>")
//
//
//                        val buttons = request.user.routing?.course_ids?.map { listOf(it.toString()) } ?: emptyList()
//                        val finalButtons = buttons + listOf(listOf("\uD83C\uDFE0 Домой"))
//
//                        request.writeButtons("<i>Введите номер курса</i>:", finalButtons)
//
//                        return EmptyRoutes queries emptyMap()
//                    }

                    request.user.updateCompletion {
                        val course = CourseMechanicumDao.findById(id)

                        it.total_processes = course?.processesCount ?: 0
                        it.course_id = id
                        it.next_process_order = 1
                        it.correct_processes = 0
                        it.processCompletions = mutableListOf<User.Completion.ProcessCompletion>()

                        it
                    }

                    return MechanicumRoutes.CHOSEN_MECHANICUM_COURSE_ID queries mapOf(
                        "course_id" to id.toString(),
                        "answer" to if(text == "да") "1" else "0",
                    )
                }
                else if(expectedRoute == MechanicumRoutes.START_MECHANICUM_COURSE) {
                    return MechanicumRoutes.START_MECHANICUM_COURSE queries mapOf(
                        "action" to "comment_added",
                        "text" to text,
                    )
                }

            Request.RequestType.COMMAND -> {}
//            null ->
//                 if(expectedRoute == MechanicumRoutes.CHOSEN_MECHANICUM_COURSE_ID) {
//                    val id = text.toInt()
//                    val ids = request.user.routing?.course_ids ?: emptyList()
//
//                    if (! ids.contains(id)) {
//                        request.writeButton("Номер курса должны быть <b>${ids.joinToString(", ")}</b>")
//
//
//                        val buttons = request.user.routing?.course_ids?.map { listOf(it.toString()) } ?: emptyList()
//                        val finalButtons = buttons + listOf(listOf("\uD83C\uDFE0 Домой"))
//
//                        request.writeButtons("<i>Введите номер курса</i>:", finalButtons)
//
//                        return EmptyRoutes queries emptyMap()
//                    }
//
//                    request.user.updateCompletion {
//                        val course = CourseMechanicumDao.findById(id)
//
//                        it.total_processes = course?.processesCount ?: 0
//                        it.course_id = id
//                        it.next_process_order = 1
//                        it.correct_processes = 0
//                        it.processCompletions = mutableListOf<User.Completion.ProcessCompletion>()
//
//                        it
//                    }
//
//                    return MechanicumRoutes.CHOSEN_MECHANICUM_COURSE_ID queries mapOf("course_id" to id.toString())
//                }
            }

        return null
    }
}