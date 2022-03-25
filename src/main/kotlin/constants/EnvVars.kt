package constants

import io.github.cdimascio.dotenv.dotenv

/**
 * Telegram bot token
 */
class EnvVars {
    companion object {
        val MECHANICUM_TELEGRAM_TOKEN: String by lazy {
            dotenv()["MECHANICUM_TOKEN"] ?: throw Exception("MECHANICUM_TOKEN not found")
        }
        val TELEGRAM_DATABASE: String by lazy {
            dotenv()["DATABASE"] ?: throw Exception("DATABASE not found")
        }
        val TELEGRAM_PASSWORD: String by lazy {
            dotenv()["PASSWORD"] ?: throw Exception("PASSWORD not found")
        }
        val TELEGRAM_USER: String by lazy {
            dotenv()["USER"] ?: throw Exception("USER not found")
        }
    }
}