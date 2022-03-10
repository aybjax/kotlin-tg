package mechanicum

import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import network.request.TgRequest

fun start(request: TgRequest) {
    val markdownV2Text = """
                    Вас приветствует ассистент телеграм бот VargatesBot
                    
                    Для продолжения *выберите продукт*
                """.trimIndent()

    val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
        listOf(InlineKeyboardButton.CallbackData(text = "Выбрать Mechanicum",
            callbackData = "mechanicum-courses")),
    )
    request.bot.sendMessage(
        chatId = request.chatid,
        text = markdownV2Text,
        parseMode = ParseMode.MARKDOWN_V2,
        replyMarkup = inlineKeyboardMarkup,
    )
}