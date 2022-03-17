package mechanicum.dto.aws_course

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * AWS Course DTO
 */
@JsonClass(generateAdapter = true)
data class CourseDto(
    @Json(name = "wd_id")
    val id: Int,
    val type: String = "",
    val name: String = "",
    val description: String = "",
    val processes_count: Int = -1,
    val animation_time: String = "",
    val category_id: Int = -1,
    val category_name: String = "",
)