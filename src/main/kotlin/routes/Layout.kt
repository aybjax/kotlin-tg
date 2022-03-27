package routes

import dataclasses.request.CallbackRequest

object Layout {
    val text:String by lazy {
        buildString {
            append(".")

            for (i in 0..1000) {
                append(' ')
            }

            append('.')
        }
    }

    /**
     * returned mostly before response
     */
    fun layoutHeader(request: CallbackRequest) {
//            if(request.needPadding) request.writeText(text, false)
    }

    /**
     * returned mostly after response
     */
    fun layoutFooter(request: CallbackRequest) {
//            request.writeButton("Вернуться домой", listOf("🏠 Домой"))
    }
}
