package network.route

import network.req_resp.CallbackRequest

/**
 * returned mostly before response
 */
fun layoutHeader(request: CallbackRequest) {
    val text = ".                                                                                                   .\n \n \n \n \n \n \n \n \n \n \n \n \n \n \n \n \n \n \n \n \n \n \n \n \n \n \n \n \n \n ."

    if(request.needPadding) request.writeText(text, false)
}
