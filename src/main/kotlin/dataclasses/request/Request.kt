package dataclasses.request

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import dataclasses.Anchor
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
                parseMode = ParseMode.HTML,
                messageId = messageId)

            return
        }

        bot.sendMessage(text = text, chatId = chatId,
            parseMode = ParseMode.HTML)
    }

    /**
     * Send user markdown? text with buttons
     */
    fun writeLink(text: String, anchors: List<Anchor>, edit: Boolean = false) {
        val btnMarkup = anchors.map {
            InlineKeyboardButton.CallbackData(text = it.text, callbackData = it.route.toQueryString())
        }

        val inlineKeyboardMarkup = InlineKeyboardMarkup.create(btnMarkup)

        if(edit) {
            bot.editMessageText(
                chatId = chatId,
                text = text,
                parseMode = ParseMode.HTML,
                replyMarkup = inlineKeyboardMarkup,
                messageId = messageId,
            )

            return
        }

        bot.sendMessage(
            chatId = chatId,
            text = text,
            parseMode = ParseMode.HTML,
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
                InlineKeyboardButton.CallbackData(text = it.text, callbackData = it.route.toQueryString())
            }
        }

        val inlineKeyboardMarkup = InlineKeyboardMarkup.create(btnMarkup)

        if(edit) {
            bot.editMessageText(
                chatId = chatId,
                text = text,
                parseMode = ParseMode.HTML,
                replyMarkup = inlineKeyboardMarkup,
                messageId = messageId,
            )

            return
        }

        bot.sendMessage(
            chatId = chatId,
            text = text,
            parseMode = ParseMode.HTML,
            replyMarkup = inlineKeyboardMarkup,
        )
    }

    /**
     * Send user markdown? text with buttons
     */
    fun writeButton(text: String, buttonTexts: List<String> = listOf("\uD83C\uDFE0 Домой"), edit: Boolean = false) {
        val btnMarkup = buttonTexts.map {
            KeyboardButton(text = it)
        }.toTypedArray()

        val keyboardMarkup = KeyboardReplyMarkup(*btnMarkup, resizeKeyboard = true)

        if(edit) {
            bot.editMessageText(
                chatId = chatId,
                text = text,
                parseMode = ParseMode.HTML,
                replyMarkup = keyboardMarkup,
                messageId = messageId,
            )

            return
        }

        bot.sendMessage(
            chatId = chatId,
            text = text,
            parseMode = ParseMode.HTML,
            replyMarkup = keyboardMarkup,
        )
    }

    fun writeButtons(text: String,
                     buttonTexts: List<List<String>> = listOf(listOf("\uD83C\uDFE0 Домой")),
                     edit: Boolean = false,
                     locationText: String? = null,
                     addHome: Boolean = false,
    ) {
        var btnMarkup = buttonTexts.map { list ->
            list.map {
                KeyboardButton(text = it)
            }
        }

        locationText?.let {
            btnMarkup = btnMarkup.toMutableList()
            (btnMarkup as MutableList<List<KeyboardButton>>).add(0, listOf(KeyboardButton(it, requestLocation = true)))
        }

        if(addHome) {
            btnMarkup = btnMarkup.toMutableList()
            (btnMarkup as MutableList<List<KeyboardButton>>).add(listOf(KeyboardButton("\uD83C\uDFE0 Домой")))
        }

        val keyboardMarkup = KeyboardReplyMarkup(btnMarkup, resizeKeyboard = true)

        if(edit) {
            bot.editMessageText(
                chatId = chatId,
                text = text,
                parseMode = ParseMode.HTML,
                replyMarkup = keyboardMarkup,
                messageId = messageId,
            )

            return
        }

        bot.sendMessage(
            chatId = chatId,
            text = text,
            parseMode = ParseMode.HTML,
            replyMarkup = keyboardMarkup,
        )
    }
}
