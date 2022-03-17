package extensions

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class StringKtTest {
    @Test
    fun `normalize lower alphanums with single space`() {
        val original = "/hEllO there       children    9f".normalizedString()
        val expected = "hello there children 9f";

        assertEquals(expected, original)
    }
}