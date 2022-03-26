package dataclasses.aws

import com.squareup.moshi.JsonClass

/**
 * AWS Process DTO
 */
@JsonClass(generateAdapter = true)
data class AwsProcessDto(
    val id: Int,
    val animation_start: Double = -1.0,
    val description: String = "",
    val detailing: String = "",
)
