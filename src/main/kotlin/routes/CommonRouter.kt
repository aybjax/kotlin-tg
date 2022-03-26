package routes

import controllers_products.*
import dataclasses.request.CallbackRequest
import routes.enums.*

object CommonRouter {
    /**
     * Route function
     */
    fun routeCallback(request: CallbackRequest) {
        when(request.route) {
            is CommonRoutes -> when(request.route as CommonRoutes) {
                    CommonRoutes.GREET_USER -> {
                        CommonController.home(request)
                    }
                    CommonRoutes.ACCOUNT_PAGE -> {
                        CommonController.redirectNotImplemented(request)
                    }
                    CommonRoutes.CHOOSE_PRODUCT -> {
                        CommonController.chooseProduct(request)
                    }
                }
            is AcademixRoutes -> when(request.route as AcademixRoutes) {
                    AcademixRoutes.ACADEMIX_COURSES -> {
                        AcademixController.listCourses(request)
                    }
                }
            is DimedusRoutes -> when(request.route as DimedusRoutes) {
                    DimedusRoutes.DIMEDUS_COURSES -> {
                        DimedusController.listCourses(request)
                    }
                }
            is RoqedRoutes -> when(request.route as RoqedRoutes) {
                    RoqedRoutes.ROQED_COURSES -> {
                        RoqedController.listCourses(request)
                    }
                    RoqedRoutes.ROQED_SEARCH_NAME -> {
                        RoqedController.searchName(request)
                    }
                    RoqedRoutes.FORWARD_ROQED_COURSES -> {
                        RoqedController.forwardCourses(request)
                    }
                    RoqedRoutes.FORWARD_ROQED_INPUT -> {
                        RoqedController.forwardInput(request)
                    }
                    RoqedRoutes.BACKWARDS_ROQED_COURSES -> {
                        RoqedController.backwordsCourses(request)
                    }
                    RoqedRoutes.BACKWARDS_ROQED_INPUT -> {
                        RoqedController.backwordsInput(request)
                    }
                    RoqedRoutes.CHOOSE_ROQED_COURSE_ID -> {
                        RoqedController.chooseCourse(request)
                    }
                    RoqedRoutes.CHOSEN_ROQED_COURSE_ID -> {
                        RoqedController.courseChosen(request)
                    }
                    RoqedRoutes.START_ROQED_COURSE -> {
                        RoqedController.startCourse(request)
                    }
                    RoqedRoutes.ROQED_SEARCH_NAME_CANCEL -> {
                        RoqedController.cancelSearch(request)
                    }
                }

            is MechanicumRoutes -> when(request.route as MechanicumRoutes) {
                    MechanicumRoutes.MECHANICUM_COURSES -> {
                        MechanicumController.listCourses(request)
                    }
                    MechanicumRoutes.MECHANICUM_SEARCH_NAME -> {
                        MechanicumController.searchName(request)
                    }
                    MechanicumRoutes.FORWARD_MECHANICUM_COURSES -> {
                        MechanicumController.forwardCourses(request)
                    }
                    MechanicumRoutes.FORWARD_MECHANICUM_INPUT -> {
                        MechanicumController.forwardInput(request)
                    }
                    MechanicumRoutes.BACKWARDS_MECHANICUM_COURSES -> {
                        MechanicumController.backwordsCourses(request)
                    }
                    MechanicumRoutes.BACKWARDS_MECHANICUM_INPUT -> {
                        MechanicumController.backwordsInput(request)
                    }
                    MechanicumRoutes.CHOOSE_MECHANICUM_COURSE_ID -> {
                        MechanicumController.chooseCourse(request)
                    }
                    MechanicumRoutes.CHOSEN_MECHANICUM_COURSE_ID -> {
                        MechanicumController.courseChosen(request)
                    }
                    MechanicumRoutes.START_MECHANICUM_COURSE -> {
                        MechanicumController.startCourse(request)
                    }
                    MechanicumRoutes.MECHANICUM_SEARCH_NAME_CANCEL -> {
                        MechanicumController.cancelSearch(request)
                    }
                }
            is EmptyRoutes -> false
        }

        if(request.route !is EmptyRoutes) {
            request.user.updateConfiguration {
                it.previous_query = request.route.toString()

                it
            }
        }
    }
}