package network.req_resp

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import db.models.User
import io.mockk.mockk
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class TgRequestTest {
    private val chatId: ChatId = mockk(relaxed = true)
    private val bot: Bot = mockk(relaxed = true)
    private val user: User = mockk(relaxed = true)

//    companion object {
//        private  val user_id: Long = mockk();
//
//        @BeforeAll
//        @JvmStatic
//        fun initDB() {
//            initDatabase(true)
//        }
//
//        @AfterAll
//        @JvmStatic
//        fun deleteUser() {
//            transaction {
//                User.find { Users.tg_user_id eq user_id }
//            }.firstOrNull()?.delete()
//        }
//    }
//
//    @Test
//    fun `route with request query`() {
//        val route = "";
//
//        val req = TgRequest(user, route, emptyMap(), bot, chatId)
//
//        assertEquals(route, req.route)
//    }
//
//    @Test
//    fun `only route companion`() {
//        val cbQuery = "route"
//        val req = TgRequest.fromCallbackUser(cbQuery, user_id, bot, chatId)
//
//        assertEquals("route", req.route)
//    }
//
//    @Test
//    fun `route and query companion`() {
//        val cbQuery = "route?hello=there"
//        val req = TgRequest.fromCallbackUser(cbQuery, user_id, bot, chatId)
//
//        assertEquals("route", req.route)
//        assertEquals("there", req.getQuery("hello"))
//    }

    @Test
    fun `route with empty query`() {
        val route = "route";

        val req = CallbackRequest(user, route, emptyMap(), bot, chatId)

        assertEquals(route, req.route)
    }

//    @Test
//    fun getQuery() {
//    }
//
//    @Test
//    fun testToString() {
//    }
//
//    @Test
//    fun getUser() {
//    }
//
//    @Test
//    fun getRoute() {
//    }
}