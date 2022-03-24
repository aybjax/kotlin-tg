package network.route

import com.github.kotlintelegrambot.entities.ParseMode
import extensions.plusOne
import extensions.roundDecimal
import db.models.mechanicum.db.models.CourseDao
import db.models.mechanicum.db.models.ProcessDao
import db.models.mechanicum.db.models.Processes
import db.models.mechanicum.listCourses
import db.models.mechanicum.home
import network.req_resp.Anchor
import network.req_resp.CallbackRequest
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.properties.Delegates

/**
 * Route function
 */
fun routeCallback(request: CallbackRequest) {
    val writeFooter = when(request.route) {
        "greet-user" -> {
            home(request)
        }

        "account-page" -> {
            redirectNotImplemented(request)

            true
        }

        "choose-product" -> {
            request.writeLink("*Выберите продукт*:", listOf(
                listOf(
                    Anchor(text = "\uD83D\uDE9C Mechanicum", link = "mechanicum-courses")
                ),
                listOf(
                    Anchor(text = "\uD83D\uDE91 Academix", link = "academix-courses")
                ),
                listOf(
                    Anchor(text = "\uD83E\uDE7A Dimedus", link = "dimedus-courses")
                ),
                listOf(
                    Anchor(text = "\uD83D\uDCDA Roqed", link = "roqed-courses")
                )
            ))

            true
        }

        "mechanicum-courses" -> {
            listCourses(request)
        }

        "mechanicum-search-name" -> {
            request.writeText("_Поиск по названию курса в Mechanicum:_")

            true
        }

        "academix-courses" -> {
            redirectNotImplemented(request)
        }

        "dimedus-courses" -> {
            redirectNotImplemented(request)
        }

        "roqed-courses" -> {
            redirectNotImplemented(request)
        }

        "forward-mechanicum-courses" -> {
            val anchors: List<List<Anchor>> = listOf(
                listOf(
                    Anchor("1", "forward-mechanicum-input?digit=1"),
                    Anchor("2", "forward-mechanicum-input?digit=2"),
                    Anchor("3", "forward-mechanicum-input?digit=3"),
                ),
                listOf(
                    Anchor("4", "forward-mechanicum-input?digit=4"),
                    Anchor("5", "forward-mechanicum-input?digit=5"),
                    Anchor("6", "forward-mechanicum-input?digit=6"),
                ),
                listOf(
                    Anchor("7", "forward-mechanicum-input?digit=7"),
                    Anchor("8", "forward-mechanicum-input?digit=8"),
                    Anchor("9", "forward-mechanicum-input?digit=9"),
                ),
                listOf(
                    Anchor("0", "forward-mechanicum-input?digit=0"),
                    Anchor("\uD83C\uDD97", "forward-mechanicum-input"),
                ),
            )
            request.writeLink("_Cтраниц для перелистывания:_", anchors)

            true
        }

        "forward-mechanicum-input" -> {
            val anchors: List<List<Anchor>> = listOf(
                listOf(
                    Anchor("1", "forward-mechanicum-input?digit=1"),
                    Anchor("2", "forward-mechanicum-input?digit=2"),
                    Anchor("3", "forward-mechanicum-input?digit=3"),
                ),
                listOf(
                    Anchor("4", "forward-mechanicum-input?digit=4"),
                    Anchor("5", "forward-mechanicum-input?digit=5"),
                    Anchor("6", "forward-mechanicum-input?digit=6"),
                ),
                listOf(
                    Anchor("7", "forward-mechanicum-input?digit=7"),
                    Anchor("8", "forward-mechanicum-input?digit=8"),
                    Anchor("9", "forward-mechanicum-input?digit=9"),
                ),
                listOf(
                    Anchor("0", "forward-mechanicum-input?digit=0"),
                    Anchor("\uD83C\uDD97", "forward-mechanicum-input"),
                ),
            )

            val digit = request.queries.get("digit")
            var currentDigit by Delegates.notNull<String>()

            digit?.let {
                request.user.updateConfiguration { configurations ->
                    val prev_digit = configurations.previous_input
                    val isCurrent = configurations.previous_input_route == "forward-mechanicum-input"

                    if(prev_digit.isNullOrBlank() && isCurrent) {
                        currentDigit = prev_digit + digit
                    }
                    else {
                        currentDigit = digit
                    }

                    configurations
                }

                request.writeLink("_Cтраниц для перелистывания:_ $currentDigit", anchors, true)
            }

            request.writeLink("_Cтраниц для перелистывания:_ no", anchors, true)

            true
        }

        "backwards-mechanicum-courses" -> {
            val anchors: List<List<Anchor>> = listOf(
                listOf(
                    Anchor("1", "backwards-mechanicum-input?digit=1"),
                    Anchor("2", "backwards-mechanicum-input?digit=2"),
                    Anchor("3", "backwards-mechanicum-input?digit=3"),
                ),
                listOf(
                    Anchor("4", "backwards-mechanicum-input?digit=4"),
                    Anchor("5", "backwards-mechanicum-input?digit=5"),
                    Anchor("6", "backwards-mechanicum-input?digit=6"),
                ),
                listOf(
                    Anchor("7", "backwards-mechanicum-input?digit=7"),
                    Anchor("8", "backwards-mechanicum-input?digit=8"),
                    Anchor("9", "backwards-mechanicum-input?digit=9"),
                ),
                listOf(
                    Anchor("0", "backwards-mechanicum-input?digit=0"),
                    Anchor("\uD83C\uDD97", "backwards-mechanicum-input"),
                ),
            )
            request.writeLink("_Cтраниц для перелистывания:_", anchors)

            true
        }

        "choose-mechanicum-course-id" -> {
            request.writeText("_Введите номер курса_:")

            true
        }

        "chosen-mechanicum-course-id" -> {
            val course = transaction {
                CourseDao.findById(request.getQuery<Int>("course_id"))
            }

            val msg = """
                        _Номер курса:_ *${course?.id}*
                        _Название курса:_ *${course?.name}*
                        _Количество процессов:_ *${course?.processesCount}*
                    """.trimIndent()

            val button = listOf(
                Anchor("Начать курс" ,"start-mechanicum-course"),
            )

            request.writeLink(msg, button)

            true
        }

        "start-mechanicum-course" -> {
            val nextOrder = request.user.configurations?.next_process_order ?: return
            val processCount = request.user.configurations?.total_processes ?: return
            val courseId = request.user.configurations?.course_id ?: return

            val process = transaction {
                ProcessDao.find {
                    (Processes.course eq courseId).and {
                        (Processes.order eq nextOrder)
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
                    CourseDao.findById(courseId)
                } ?: return

                val configurations = request.user.configurations
                val correct = configurations?.correct_processes ?: 0
                val total = configurations?.total_processes ?: -1

                request.writeText("*Курс пройден*: $correct из $total правильных")
                request.writeText("${(correct.toDouble()/total.toDouble() * 100).roundDecimal()}")

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
                        *${process?.description?.trim()}*
                        ${process?.detailing?.trim()}
                    """.trimIndent()

                val buttons = listOf(
                    Anchor("Сделано", "start-mechanicum-course?action=done"),
                    Anchor("Пропустить", "start-mechanicum-course")
                )

                request.writeLink(msg, buttons)
            }

            true
        }
        "mechanicum-search-name-cancel" -> {
            request.user.updateConfiguration {
                it.searchName = null

                it
            }

            request.updateRouteQuery("mechanicum-courses")

            routeCallback(request)

            false
        }
        else -> false
    }

    if(writeFooter) Layout.layoutFooter(request)

    if(! request.route.isNullOrBlank()) {
        request.user.updateConfiguration {
            it.previous_query = request.route

            it
        }
    }
}