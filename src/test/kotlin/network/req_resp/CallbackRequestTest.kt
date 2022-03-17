package network.req_resp

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import db.models.User
import io.mockk.mockk
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class CallbackRequestTest {

    @Test
    fun `get query with type`() {
        val bot: Bot = mockk()
        val chatId: ChatId = mockk()
        val user: User = mockk()

        var cb = CallbackRequest(
            user,
            "",
            mapOf("bir" to "1"),
            bot,
            chatId,
        )

        val result = cb.getQuery<Int>("bir")

        assertEquals(1, result)
    }
}