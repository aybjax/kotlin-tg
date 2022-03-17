package mechanicum.dto.aws_course

import com.squareup.moshi.JsonClass

/**
 * AWS Process DTO
 */
@JsonClass(generateAdapter = true)
data class ProcessDto(
    val id: Int,
    val animation_start: Double = -1.0,
    val description: String = "",
    val detailing: String = "",
)
