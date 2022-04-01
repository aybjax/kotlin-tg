package variables

/**
 * Telegram bot token
 */
class DatabaseTelegramEnvVars {
    enum class OS {
        WINDOWS, LINUX,
    }

    companion object {
        val os: OS by lazy {
            val tmp = System.getProperty("os.name").lowercase()

            when {
                tmp.contains("win") -> OS.WINDOWS
                else -> OS.LINUX
            }
        }

        fun checkArgs() {
            Thread.sleep(1000L)

            println("""
                TELEGRAM_TOKEN=$TELEGRAM_TOKEN
                DATABASE=$DATABASE
                PASSWORD=$PASSWORD
                USER=$USER
                AYBJAXDIMEDUS=$AYBJAXDIMEDUS
            """.trimIndent())

            Thread.sleep(1000L)
        }

        val TELEGRAM_TOKEN: String by lazy {
            when(os) {
                OS.WINDOWS -> "5236277535:AAGGYiBH3AKyAAAWgLCPwk_38OZZuQx1e20"
                OS.LINUX -> "5127502789:AAGcpHYI-2HesaZq8y6cCLVRRsC0S1svgH8"
            }
        }
        val DATABASE: String by lazy {
            when(os) {
                OS.WINDOWS -> "kotlintg"
                OS.LINUX -> "telegrambot"
            }
        }
        val PASSWORD: String by lazy {
            when(os) {
                OS.WINDOWS -> "aybjax"
                OS.LINUX -> "Number1Dimedus"
            }
        }
        val USER: String by lazy {
            when(os) {
                OS.WINDOWS -> "root"
                OS.LINUX -> "telegrambot"
            }
        }
        const val AYBJAXDIMEDUS: Long = 1584447386
    }
}