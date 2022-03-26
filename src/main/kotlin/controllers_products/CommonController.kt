package controllers_products

import db.models.User
import dataclasses.Anchor
import dataclasses.request.CallbackRequest
import dataclasses.request.Request
import dataclasses.RouteQueryPair
import org.jetbrains.exposed.sql.transactions.transaction
import routes.enums.*

object CommonController {
    /**
     *
     */
    fun redirectNotImplemented(request: CallbackRequest): Boolean {
        request.writeButton("*Доступ только для разработчиков*")
        home(request)

        return false
    }

    /**
     * Welcome message
     */
    fun home(request: Request): Boolean {
        val text = "Вас приветствует ассистент телеграм бот *VargatesBot*"

        request.writeLink(text, listOf(
            listOf(Anchor(text = "\uD83D\uDC64 Личный кабинет",  RouteQueryPair(CommonRoutes.ACCOUNT_PAGE))),
            listOf(Anchor(text = "▶ Выбрать Продукт", RouteQueryPair(CommonRoutes.CHOOSE_PRODUCT)))
        ))

        transaction {
            request.user.updateConfiguration { User.Configurations() }
        }

        request.writeButton("Можно вернуться на эту страницу нажав кнопку снизу", listOf("\uD83C\uDFE0 Домой"))

        return false;
    }

    fun chooseProduct(request: CallbackRequest): Boolean {
        request.writeLink("*Выберите продукт*:", listOf(
            listOf(
                Anchor(text = "\uD83D\uDE9C Mechanicum", RouteQueryPair(MechanicumRoutes.MECHANICUM_COURSES))
            ),
            listOf(
                Anchor(text = "\uD83D\uDE91 Academix", RouteQueryPair(AcademixRoutes.ACADEMIX_COURSES))
            ),
            listOf(
                Anchor(text = "\uD83E\uDE7A Dimedus", RouteQueryPair(DimedusRoutes.DIMEDUS_COURSES))
            ),
            listOf(
                Anchor(text = "\uD83D\uDCDA Roqed", RouteQueryPair(RoqedRoutes.ROQED_COURSES))
            )
        ))

        return true
    }
}