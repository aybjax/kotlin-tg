package extensions

fun String.shrink(): String {
    return this.replace("\n", "").
            replace("\t", "")
}

fun String.normalizedWord(): String {
    return this.split(" ")[0].lowercase()
}

fun String.tryInt(): Int? {
    return try {
        this.toInt()
    }
    catch (e: NumberFormatException) {
        null
    }
}

fun String.intOrDefault(): Int {
    return try {
        this.toInt()
    }
    catch (e: NumberFormatException) {
        0
    }
}

fun String.tryLong(): Long? {
    return try {
        this.toLong()
    }
    catch (e: NumberFormatException) {
        null
    }
}

fun String.longOrDefault(): Long? {
    return try {
        this.toLong()
    }
    catch (e: NumberFormatException) {
        0L
    }
}