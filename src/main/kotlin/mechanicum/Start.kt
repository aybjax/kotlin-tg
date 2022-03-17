package mechanicum

import db.models.User
import network.req_resp.Anchor
import network.req_resp.Request
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Welcome message
 */
fun home(request: Request): Boolean {
    val text = "Вас приветствует ассистент телеграм бот *VargatesBot*"

    request.writeLink(text, listOf(
        Anchor(text = "Личный кабинет", link = "account-page"),
        Anchor(text = "Выбрать Продукт", link = "choose-product"),
    ))

    transaction {
        request.user.updateConfiguration { User.Configurations() }
    }

    return false;
}