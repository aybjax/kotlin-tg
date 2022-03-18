package network.req_resp

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import db.models.User
import extensions.getFirstWord
import extensions.normalizedString
import mechanicum.db.models.CourseDao

/**
 * Request object for text and commands
 *  Converted to CallbackRequest
 */
class TextRequest(
    override val user: User,
    text: String,
    override val bot: Bot,
    override val chatId: ChatId,
): Request(user, bot, chatId)
{
    val text = text.normalizedString()
    private val greetingWords = listOf("start", "начать");

    /**
     * Convert to CallbackRequest
     */
    fun toCallbackRequest(): CallbackRequest {
        val query = getCallbackQuery();

        return CallbackRequest.fromCallback(
            query,
            user,
            bot,
            chatId,
        )
    }

    /**
     * Create route (a?b=c) dependent on text written
     */
    private fun getCallbackQuery(): String {
        val firstWord = text.getFirstWord()
        val previousQuery = user.configurations?.previous_query ?: ""

        if(firstWord.isEmpty()) return ""

        if(greetingWords.contains(firstWord)) return "greet-user";

        if(previousQuery == "mechanicum-search-name") {
            user.updateConfiguration {
                it.searchName = text.split(' ').joinToString("%", prefix = "%", postfix = "%")

                it
            }

            return "mechanicum-courses"
        }

        if(previousQuery == "backwards-mechanicum-courses") {
            val page = (user.configurations?.prev_page ?: 1) - text.toLong()

            return "mechanicum-courses?page=$page"
        }

        if(previousQuery == "forward-mechanicum-courses") {
            val page = (user.configurations?.prev_page ?: 1) + text.toLong()

            return "mechanicum-courses?page=$page"
        }

        if(previousQuery == "choose-mechanicum-course-id") {
            val id = text.toInt()
            val ids = user.configurations?.course_ids ?: emptyList()

            if (! ids.contains(id)) {
                writeText("Номер курса должны быть *${ids.joinToString(", ")}*")

                return ""
            }

            user.updateConfiguration {
                val course = CourseDao.findById(id)

                it.total_processes = course?.processesCount ?: 0
                it.course_id = id
                it.next_process_order = 1
                it.correct_processes = 0

                it
            }

            return "chosen-mechanicum-course-id?course_id=$id"
        }

        return ""
    }


    companion object {
        /**
         * Create TextRequest from text and User ID
         */
        fun fromTextUser(text: String, userDto: User.About, bot: Bot, chatId: ChatId): TextRequest {
            val user = User.getUser(userDto)

            return TextRequest(user, text, bot, chatId)
        }
    }
}
