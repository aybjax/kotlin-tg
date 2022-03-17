package network.route

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import network.req_resp.Anchor
import network.req_resp.CallbackRequest

/**
 * returned mostly after response
 */
fun layoutFooter(request: CallbackRequest) {
    request.writeLink("Вернуться домой", listOf(Anchor(text = "Домой", link = "greet-user")))
}
