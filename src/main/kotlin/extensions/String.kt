package extensions

/**
 * Removes tabs and newlines
 */
fun String.shrink(): String {
    return this.replace("\n", "").
            replace("\t", "")
}

/**
 * Lower case and alpha(cyrillic/latin)-num
 */
fun String.normalizedString(): String {
    val re = "[^a-zа-я0-9]".toRegex()

    return this.split(" ").filter {
        it.isNotEmpty()
    }.joinToString(" ") {
        re.replace(it.lowercase(), "")
    }
}

/**
 * get the first word of the sentence
 */
fun String.getFirstWord(): String {
    val words = this.split(' ')
    return if(words.isNotEmpty()) words[0] else ""
}

/**
 * Return int or null
 */
fun String.tryInt(): Int? {
    return try {
        this.toInt()
    }
    catch (e: NumberFormatException) {
        null
    }
}

/**
 * Returns int
 */
fun String.intOrDefault(): Int {
    return try {
        this.toInt()
    }
    catch (e: NumberFormatException) {
        0
    }
}

/**
 * Return long or null
 */
fun String.tryLong(): Long? {
    return try {
        this.toLong()
    }
    catch (e: NumberFormatException) {
        null
    }
}

/**
 * Returns long
 */
fun String.longOrDefault(): Long? {
    return try {
        this.toLong()
    }
    catch (e: NumberFormatException) {
        0L
    }
}