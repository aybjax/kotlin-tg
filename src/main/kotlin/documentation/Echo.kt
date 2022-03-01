package documentation

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import constants.TELEGRAM_TOKEN

fun runEchoExample() {

    val bot = bot {

        token = TELEGRAM_TOKEN

        dispatch {

            text {
                bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = text)
            }
        }
    }

    bot.startPolling()
}