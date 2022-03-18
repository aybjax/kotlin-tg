package network.route

import network.req_resp.Anchor
import network.req_resp.CallbackRequest

class Layout {
    companion object {
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
            if(request.needPadding) request.writeText(text, false)
        }

        /**
         * returned mostly after response
         */
        fun layoutFooter(request: CallbackRequest) {
            request.writeLink("Вернуться домой", listOf(Anchor(text = "Домой", link = "greet-user")))
        }
    }
}
