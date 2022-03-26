package dataclasses

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import routes.enums.MechanicumRoutes

internal class RouteQueryPairTest {
    @Test
    fun `RouteQueryPair to String`() {
        val obj = RouteQueryPair(
            MechanicumRoutes.MECHANICUM_COURSES,
            mapOf("a" to "b", "c" to "d")
        )

        assertEquals(
            obj.toQueryString(),
            "${MechanicumRoutes.MECHANICUM_COURSES}?a=b&c=d"
        )
    }
}