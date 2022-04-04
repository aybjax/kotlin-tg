package controllers_products

import com.github.kotlintelegrambot.entities.TelegramFile
import com.itextpdf.io.font.FontProgramFactory
import com.itextpdf.kernel.colors.Color
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import dataclasses.Anchor
import dataclasses.RequestPage
import dataclasses.RouteQueryPair
import dataclasses.queries
import dataclasses.request.CallbackRequest
import dataclasses.request.Request
import dataclasses.request.TextRequest
import db.models.CourseRoqedDao
import db.models.ProcessRoqedDao
import db.models.Processes_Roqed
import db.models.User
import extensions.plusOne
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import routes.CommonRouter
import routes.enums.RoqedRoutes
import routes.enums.Routes
import java.io.File


object RoqedController {
    /**
     * List mechanicum courses: search by name and paginate
     */
    fun listCourses(request: CallbackRequest): Boolean {
        val (courses, page, pageCount) = CourseRoqedDao.getCoursePageCount(
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
                        RoqedRoutes.BEFORE_CHOOSEN_ROQED_COURSE_ID queries mapOf("course_id" to it.id.toString())
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
                RoqedRoutes.BEFORE_CHOOSEN_ROQED_COURSE_ID queries mapOf("course_id" to courses.last().id.toString())
            )
        )

        val anchors = mutableListOf<Anchor>()

        if(page.isNotFirstPage()) {
            anchors.add(
                Anchor(text = "⬅", RoqedRoutes.ROQED_COURSES queries mapOf("page" to page.prev.toString()))
            )
        }

        if(page notLastPageFor pageCount) {
            anchors.add(
                Anchor(text = "➡", RoqedRoutes.ROQED_COURSES queries mapOf("page" to page.next.toString()))
            )
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
            request.writeLink("<b>Ничего не найдено</b>", jumpButtons)
        }

        val buttons = mutableListOf(
            Anchor(text = "\uD83D\uDD0D Поиск по названию", RouteQueryPair(RoqedRoutes.ROQED_SEARCH_NAME))
        )

        if(! request.user.routing?.searchName.isNullOrEmpty()) {
            buttons.add(Anchor(text = "❌ Отменить поиск", RouteQueryPair(RoqedRoutes.ROQED_SEARCH_NAME_CANCEL)))
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
        request.writeButton("<i>Поиск по названию курса в Roqed:</i>")
        request.user.updateRouting {
            it.expectedQuery = User.Routing.ExpectedQuery(
                route = RoqedRoutes.ROQED_COURSES.toString(),
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
                route = RoqedRoutes.ROQED_COURSES.toString(),
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
                route = RoqedRoutes.ROQED_COURSES.toString(),
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
                route = RoqedRoutes.CHOSEN_ROQED_COURSE_ID.toString(),
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
                route = RoqedRoutes.CHOSEN_ROQED_COURSE_ID.toString(),
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
            CourseRoqedDao.findById(request.getQuery<Int>("course_id"))
        }

        val msg = """
                            <i>Номер курса:</i> <b>${course?.id}</b>
                            <i>Название курса:</i> <b>${course?.name}</b>
                            <i>Количество процессов:</i> <b>${course?.processesCount}</b>
                        """.trimIndent()

        if ((course?.processesCount ?: 0) > 0) {
            val button = listOf(
                Anchor("Начать" , RouteQueryPair(RoqedRoutes.START_ROQED_COURSE)),
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
            ProcessRoqedDao.find {
                (Processes_Roqed.course eq courseId).and {
                    (Processes_Roqed.order eq nextOrder)
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
                        route = RoqedRoutes.START_ROQED_COURSE.toString(),
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
                        route = RoqedRoutes.START_ROQED_COURSE.toString(),
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
                    request.writeButton("Ваш комментарий записан \uD83D\uDC4D")

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
                CourseRoqedDao.findById(courseId)
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

            val course = transaction {
                CourseRoqedDao.findById(courseId)
            }


            val fontProgramThick = FontProgramFactory.createFont("asset/font/normal.ttf")
            val fontThick = PdfFontFactory.createFont(fontProgramThick)
            val fontProgramThin = FontProgramFactory.createFont("asset/font/regular.ttf")
            val fontThin = PdfFontFactory.createFont(fontProgramThin)

            val filename = "${request.user.userId}.pdf"
            val pdfWriter = PdfWriter(filename)
            val pdfDocument = PdfDocument(pdfWriter)
            pdfDocument.addNewPage()

            val document = Document(pdfDocument)

            val paragraph1 = Paragraph("""
                Прохождение курса ${course?.name ?: ""}
                ${request.user.about?.firstName ?: ""} ${request.user.about?.lastName ?: ""} (${request.user.about?.username ?: ""})
            """.trimIndent())
            paragraph1.setFont(fontThick)
            paragraph1.setTextAlignment(TextAlignment.CENTER)
            paragraph1.setFontSize(22f)
            document.add(paragraph1)

            request.user.completion?.location?.let {
                val paragraph = Paragraph();
                val text1 = Text("Место прохождения: ")
                text1.setFont(fontThick)
                val text2 = Text(it)
                text2.setFont(fontThin)
                text2.setFontColor(ColorConstants.BLACK)
                paragraph.add(text1)
                paragraph.add(text2)
                document.add(paragraph)
            }


            val paragraph2 = Paragraph();
            val text1 = Text("Сделано:")
            text1.setFont(fontThick)
            val text2 = Text(" $correct из $total")
            text2.setFont(fontThin)
            text2.setFontColor(ColorConstants.BLACK)
            paragraph2.add(text1)
            paragraph2.add(text2)
            document.add(paragraph2)

            request.user.completion?.processCompletions?.filter {
                it.status == User.Completion.CompletionStatus.FAIL
            }?.let { fails ->
                if(fails.isNotEmpty()) {
                    val paragraph2 = Paragraph("""
                        Пропущенные процессы
                    """.trimIndent())
                    paragraph2.setMarginTop(25f)
                    paragraph2.setFont(fontThick)
                    paragraph2.setTextAlignment(TextAlignment.CENTER)
                    paragraph2.setFontSize(18f)
                    document.add(paragraph2)

                    val table = Table(2)
                    table.addHeaderCell("Название процесса")
                    table.addHeaderCell("Комментарий")
                    table.header.setFont(fontThick)
                    table.setFont(fontThin)
                    table.setFontColor(ColorConstants.BLACK)

                    fails.forEach {
                        if (!it.comment.isNullOrBlank()) {
                            table.addCell("${it.process_order}. ${it.process_name}");
                            table.addCell("${it.comment?.split('\n')?.joinToString("\n") ?: ""}");
                        }

                        document.add(table)
                    }
                }
            }

            request.user.completion?.processCompletions?.filterNot {
                it.status == User.Completion.CompletionStatus.FAIL ||
                        it.comment.isNullOrBlank()
            }?.let { withComments ->
                if(withComments.isNotEmpty()) {

                    val paragraph2 = Paragraph("""
                        Оставили комментарий
                    """.trimIndent())
                    paragraph2.setMarginTop(25f)
                    paragraph2.setFontSize(18f)
                    paragraph2.setTextAlignment(TextAlignment.CENTER)
                    paragraph2.setFont(fontThick)
                    document.add(paragraph2)

                    val table = Table(2)
                    table.addHeaderCell("Название процесса")
                    table.addHeaderCell("Комментарий")
                    table.header.setFont(fontThick)
                    table.setFont(fontThin)
                    table.setFontColor(ColorConstants.BLACK)

                    withComments.forEach {
                        if (!it.comment.isNullOrBlank()) {
                            table.addCell("${it.process_order}. ${it.process_name}");
                            table.addCell("${it.comment?.split('\n')?.joinToString("\n") ?: ""}");
                        }
                    }

                    document.add(table)

                }
            }


            document.close()

            request.bot.sendDocument(request.chatId,
                TelegramFile.ByFile(File(filename)),
                caption = "Ваши результаты")
            File(filename).delete()
        }
        else {
            val msg = """
                            $nextOrder.
                            <b>${process?.description?.trim()}</b>:
                            ${process?.detailing?.trim()}
                        """.trimIndent()

            val buttons = listOf(
                listOf(
                    Anchor("Сделано", RoqedRoutes.START_ROQED_COURSE queries mapOf("action" to "done")),
                    Anchor("Пропустить", RoqedRoutes.START_ROQED_COURSE queries mapOf("action" to "fail")),
                ),
                listOf(
                    Anchor("Комментарий", RoqedRoutes.START_ROQED_COURSE queries mapOf("action" to "comment")),
                    Anchor("Закончить", RoqedRoutes.START_ROQED_COURSE queries mapOf("action" to "end")),
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

    fun textToCallbackQuery(expectedRoute: Routes, text: String, request: TextRequest): RouteQueryPair? {
        val expectedQuery = request.user.routing!!.expectedQuery!!

        when(expectedQuery.requestType) {
            Request.RequestType.TEXT ->
                if (expectedRoute == RoqedRoutes.ROQED_COURSES) {
                    if (expectedQuery.action == User.Routing.ExpectedQuery.Action.SEARCH_NAME) {
                        request.user.updateRouting {
                            it.searchName = text.split(' ').joinToString("%", prefix = "%", postfix = "%")

                            it
                        }

                        return RoqedRoutes.ROQED_COURSES queries emptyMap()
                    } else if (expectedQuery.action == User.Routing.ExpectedQuery.Action.BACKWARDS_PAGING) {
                        val page = (request.user.routing?.prev_page ?: 1) - text.toLong()

                        return RoqedRoutes.ROQED_COURSES queries mapOf("page" to page.toString())
                    } else if (expectedQuery.action == User.Routing.ExpectedQuery.Action.FORWARD_PAGING) {
                        val page = (request.user.routing?.prev_page ?: 1) + text.toLong()

                        return RoqedRoutes.ROQED_COURSES queries mapOf("page" to page.toString())
                    }
                } else if (expectedRoute == RoqedRoutes.CHOSEN_ROQED_COURSE_ID) {
                    // FIXME
                    val id = (expectedQuery.payload as String).toInt()

                    request.user.updateCompletion {
                        val course = CourseRoqedDao.findById(id)

                        it.total_processes = course?.processesCount ?: 0
                        it.course_id = id
                        it.next_process_order = 1
                        it.correct_processes = 0
                        it.processCompletions = mutableListOf<User.Completion.ProcessCompletion>()

                        it
                    }

                    return RoqedRoutes.CHOSEN_ROQED_COURSE_ID queries mapOf(
                        "course_id" to id.toString(),
                        "answer" to if (text == "да") "1" else "0",
                    )
                } else if (expectedRoute == RoqedRoutes.START_ROQED_COURSE) {
                    return RoqedRoutes.START_ROQED_COURSE queries mapOf(
                        "action" to "comment_added",
                        "text" to text,
                    )
                }

            Request.RequestType.COMMAND -> {}
        }

        return null
    }
}