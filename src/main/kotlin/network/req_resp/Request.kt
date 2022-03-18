package network.req_resp

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import db.models.User
import examples.telegramsamples.generateUsersButton

/**
 * Base class for Requests
 */
sealed class Request(
    open val user: User,
    open val bot: Bot,
    open val chatId: ChatId,
) {
    /**
     * Send user markdown? text message
     */
    fun writeText(text: String, isMarkdown: Boolean = true) {
        if (isMarkdown) {
            bot.sendMessage(text = text, chatId = chatId, parseMode = ParseMode.MARKDOWN_V2)

            return
        }

        bot.sendMessage(text = text, chatId = chatId)
    }

    /**
     * Send user markdown? text with buttons
     */
    fun writeLink(text: String, anchors: List<Anchor>, isParsed: Boolean = true) {
        val btnMarkup = anchors.map {
            InlineKeyboardButton.CallbackData(text = it.text, callbackData = it.link)
        }

        val inlineKeyboardMarkup = InlineKeyboardMarkup.create(btnMarkup)

        if(isParsed) {
            bot.sendMessage(
                chatId = chatId,
                text = text,
                parseMode = ParseMode.MARKDOWN,
                replyMarkup = inlineKeyboardMarkup,
            )

            return
        }


        bot.sendMessage(
            chatId = chatId,
            text = text,
            replyMarkup = inlineKeyboardMarkup,
        )
    }

    /**
     * Send user markdown? text with buttons
     */
    @JvmName("writeLink1")
    fun writeLink(text: String, anchors: List<List<Anchor>>, isParsed: Boolean = true) {
        val btnMarkup = anchors.map { list ->
            list.map {
                InlineKeyboardButton.CallbackData(text = it.text, callbackData = it.link)
            }
        }

        val inlineKeyboardMarkup = InlineKeyboardMarkup.create(btnMarkup)

        if(isParsed) {
            bot.sendMessage(
                chatId = chatId,
                text = text,
                parseMode = ParseMode.MARKDOWN,
                replyMarkup = inlineKeyboardMarkup,
            )

            return
        }


        bot.sendMessage(
            chatId = chatId,
            text = text,
            replyMarkup = inlineKeyboardMarkup,
        )
    }

    /**
     * Send user markdown? text with buttons
     */
    fun writeButton(text: String, buttonTexts: List<String>, isParsed: Boolean = true) {
        val btnMarkup = buttonTexts.map {
            KeyboardButton(text = it)
        }.toTypedArray()

        val keyboardMarkup = KeyboardReplyMarkup(*btnMarkup, resizeKeyboard = true)

        if(isParsed) {
            bot.sendMessage(
                chatId = chatId,
                text = text,
                parseMode = ParseMode.MARKDOWN,
                replyMarkup = keyboardMarkup
            )

            return
        }

        bot.sendMessage(
            chatId = chatId,
            text = text,
            replyMarkup = keyboardMarkup
        )
    }

    @JvmName("writeButton1")
    fun writeButton(text: String, buttonTexts: List<List<String>>, isParsed: Boolean = true) {
        val btnMarkup = buttonTexts.map { list ->
            list.map {
                KeyboardButton(text = it)
            }
        }

        val keyboardMarkup = KeyboardReplyMarkup(btnMarkup, resizeKeyboard = true)

        if(isParsed) {
            bot.sendMessage(
                chatId = chatId,
                text = text,
                parseMode = ParseMode.MARKDOWN,
                replyMarkup = keyboardMarkup
            )

            return
        }

        bot.sendMessage(
            chatId = chatId,
            text = text,
            replyMarkup = keyboardMarkup
        )
    }
}
