package network.route

import db.models.mechanicum.home
import network.req_resp.CallbackRequest

/**
 *
 */
fun redirectNotImplemented(request: CallbackRequest): Boolean {
    request.writeText("*Доступ только для разработчиков*")

    home(request)

    return false
}