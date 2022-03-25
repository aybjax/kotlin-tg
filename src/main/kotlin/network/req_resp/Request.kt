package network.req_resp

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import db.models.User

/**
 * Base class for Requests
 */
sealed class Request(
    open val user: User,
    open val bot: Bot,
    open val chatId: ChatId,
    open val messageId: Long,
) {
    /**
     * Send user markdown? text message
     */
    fun writeText(text: String, edit: Boolean = false) {
        if(edit) {
            bot.editMessageText(text = text, chatId = chatId,
                parseMode = ParseMode.MARKDOWN_V2,
                messageId = messageId)

            return
        }

        bot.sendMessage(text = text, chatId = chatId,
            parseMode = ParseMode.MARKDOWN_V2)
    }

    /**
     * Send user markdown? text with buttons
     */
    fun writeLink(text: String, anchors: List<Anchor>, edit: Boolean = false) {
        val btnMarkup = anchors.map {
            InlineKeyboardButton.CallbackData(text = it.text, callbackData = it.link)
        }

        val inlineKeyboardMarkup = InlineKeyboardMarkup.create(btnMarkup)

        if(edit) {
            bot.editMessageText(
                chatId = chatId,
                text = text,
                parseMode = ParseMode.MARKDOWN,
                replyMarkup = inlineKeyboardMarkup,
                messageId = messageId,
            )

            return
        }

        bot.sendMessage(
            chatId = chatId,
            text = text,
            parseMode = ParseMode.MARKDOWN,
            replyMarkup = inlineKeyboardMarkup,
        )
    }

    /**
     * Send user markdown? text with buttons
     */
    @JvmName("writeLink1")
    fun writeLink(text: String, anchors: List<List<Anchor>>, edit: Boolean = false) {
        val btnMarkup = anchors.map { list ->
            list.map {
                InlineKeyboardButton.CallbackData(text = it.text, callbackData = it.link)
            }
        }

        val inlineKeyboardMarkup = InlineKeyboardMarkup.create(btnMarkup)

        if(edit) {
            bot.editMessageText(
                chatId = chatId,
                text = text,
                parseMode = ParseMode.MARKDOWN,
                replyMarkup = inlineKeyboardMarkup,
                messageId = messageId,
            )

            return
        }

        bot.sendMessage(
            chatId = chatId,
            text = text,
            parseMode = ParseMode.MARKDOWN,
            replyMarkup = inlineKeyboardMarkup,
        )
    }

    /**
     * Send user markdown? text with buttons
     */
    fun writeButton(text: String, buttonTexts: List<String> = listOf("Домой"), edit: Boolean = false) {
        val btnMarkup = buttonTexts.map {
            KeyboardButton(text = it)
        }.toTypedArray()

        val keyboardMarkup = KeyboardReplyMarkup(*btnMarkup, resizeKeyboard = true)

        if(edit) {
            bot.editMessageText(
                chatId = chatId,
                text = text,
                parseMode = ParseMode.MARKDOWN,
                replyMarkup = keyboardMarkup,
                messageId = messageId,
            )

            return
        }

        bot.sendMessage(
            chatId = chatId,
            text = text,
            parseMode = ParseMode.MARKDOWN,
            replyMarkup = keyboardMarkup,
        )
    }

    fun writeButtons(text: String, buttonTexts: List<List<String>> = listOf(listOf("Домой")), edit: Boolean = false) {
        val btnMarkup = buttonTexts.map { list ->
            list.map {
                KeyboardButton(text = it)
            }
        }

        val keyboardMarkup = KeyboardReplyMarkup(btnMarkup, resizeKeyboard = true)

        if(edit) {
            bot.editMessageText(
                chatId = chatId,
                text = text,
                parseMode = ParseMode.MARKDOWN,
                replyMarkup = keyboardMarkup,
                messageId = messageId,
            )

            return
        }

        bot.sendMessage(
            chatId = chatId,
            text = text,
            parseMode = ParseMode.MARKDOWN,
            replyMarkup = keyboardMarkup,
        )
    }
}
