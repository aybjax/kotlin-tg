package examples.telegramsamples

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import containers.EnvVars

fun runEchoExample() {

    val bot = bot {

        token = EnvVars.TELEGRAM_TOKEN

        dispatch {

            text {
                bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = text)
            }
        }
    }

    bot.startPolling()
}