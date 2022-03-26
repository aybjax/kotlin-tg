package dataclasses.aws

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * AWS Course DTO
 */
@JsonClass(generateAdapter = true)
data class AwsCourseDto(
    @Json(name = "wd_id")
    val id: Int,
    val type: String = "",
    val name: String = "",
    val description: String = "",
    val processes_count: Int = -1,
    val animation_time: String = "",
)