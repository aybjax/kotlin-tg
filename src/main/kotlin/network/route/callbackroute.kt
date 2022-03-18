package network.route

import com.github.kotlintelegrambot.entities.ParseMode
import extensions.plusOne
import extensions.roundDecimal
import mechanicum.db.models.CourseDao
import mechanicum.db.models.ProcessDao
import mechanicum.db.models.Processes
import mechanicum.listCourses
import mechanicum.home
import network.req_resp.Anchor
import network.req_resp.CallbackRequest
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

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
            request.writeText("Выберите продукт:")
            request.writeLink("*Mechanicum*", listOf(
                Anchor(text = "Выбрать Mechanicum", link = "mechanicum-courses")
            ))
            request.writeLink("*Academix*", listOf(
                Anchor(text = "Выбрать Academix", link = "academix-courses")
            ))
            request.writeLink("*Dimedus*", listOf(
                Anchor(text = "Выбрать Dimedus", link = "dimedus-courses")
            ))
            request.writeLink("*Roqed*", listOf(
                Anchor(text = "Выбрать Roqed", link = "roqed-courses")
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

        "forward-mechanicum-courses",
        "backwards-mechanicum-courses" -> {
            //FIXME not sent
            request.writeText("_Cтраниц для перелистывания:_")

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