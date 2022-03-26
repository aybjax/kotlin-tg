package controllers_products

import dataclasses.request.CallbackRequest

object AcademixController {
    fun listCourses(request: CallbackRequest): Boolean {
        CommonController.redirectNotImplemented(request)

        return true
    }

}
