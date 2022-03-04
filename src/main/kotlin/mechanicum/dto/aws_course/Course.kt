package mechanicum.dto.aws_course

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Course(
    @Json(name = "wd_id")
    val id: Int,
    val type: String,
    val name: String,
    val description: String,
    val processes_count: Int,
    val animation_time: String,
    val category_id: Int,
    val category_name: String,
)