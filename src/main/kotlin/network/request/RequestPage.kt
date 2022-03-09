package network.request

@JvmInline
value class RequestPage(val value: Long) {
    companion object {
        fun fromQuery(pageNumber: String?): RequestPage {
            var page = pageNumber?.toLong() ?: 1;

            if(page < 1) page = 1;

            return RequestPage(page)
        }
    }

    inline fun isFirstPage() = value == 1L

    inline fun isNextLastPage(pageCount: Long) = pageCount == value + 1

    val next get() = value + 1
    val prev get() = value - 1
    // FIXME should be separate or in user configurations
    val sqlLimit get() = 5
    val sqlOffset get() = (value - 1) * sqlLimit

    inline fun getTotalPageCount(itemsCount: Long) = itemsCount / 5 + if(itemsCount % 5 > 0) 2 else 1
}
