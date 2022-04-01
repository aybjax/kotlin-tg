package extensions

import kotlin.math.roundToInt

/**
 * Rounds Double to tenth
 */
fun Double.roundDecimal(): Double {
    return (this * 10.0).roundToInt() / 10.0
}

fun Double.kmToDegree(): Double {
    return this/111;
}

fun Double.mToDegree(): Double {
    return (this * 1000).kmToDegree()
}

fun Double.degreeToKm(): Double {
    return this * 111
}

fun Double.degreeToM(): Double {
    return (this * 1000).degreeToKm()
}