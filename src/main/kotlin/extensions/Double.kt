package extensions

import kotlin.math.roundToInt

fun Double.roundDecimal(): Double {
    return (this * 10.0).roundToInt() / 10.0
}