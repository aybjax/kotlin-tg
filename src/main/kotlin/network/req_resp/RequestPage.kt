package network.req_resp

import extensions.tryLong

/**
 * Long with associate functions
 */
@JvmInline
value class RequestPage(val value: Long = 1L) {
    companion object {
        /**
         * Create from string
         */
        fun fromString(pageNumber: String?): RequestPage {
            var page = pageNumber?.tryLong() ?: 1;

            if(page < 1) page = 1;

            return RequestPage(page)
        }
    }

    /**
     * is it 1
     */
    fun isFirstPage() = value == 1L

    /**
     * is not 1
     */
    fun isNotFirstPage() = ! isFirstPage()

    /**
     * pageCount <= value + 1
     */
    infix fun lastPageFor(pageCount: Long) = pageCount <= value + 1

    /**
     * not pageCount <= value + 1
     */
    infix fun notLastPageFor(pageCount: Long) = ! lastPageFor(pageCount)

    val next get() = value + 1
    val prev get() = value - 1
    // FIXME should be separate or in user configurations
    val sqlLimit get() = 5
    val sqlOffset get() = (value - 1) * sqlLimit
    fun getTotalPageCount(itemsCount: Long) = itemsCount / 5 + if(itemsCount % 5 > 0) 2 else 1

    operator fun dec(): RequestPage {
        return RequestPage(value-1)
    }

    operator fun inc(): RequestPage {
        return RequestPage(value+1)
    }

    /**
     * Avoid overshooting
     */
    infix fun makeLessThan(pageCount: Long) = if(value >= pageCount) {
        RequestPage(pageCount-1)
    } else {
        this
    }
}
