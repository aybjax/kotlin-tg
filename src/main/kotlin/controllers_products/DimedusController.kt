package controllers_products

import dataclasses.request.CallbackRequest

object DimedusController {
    fun listCourses(request: CallbackRequest): Boolean {
        CommonController.redirectNotImplemented(request)

        return true
    }

}
