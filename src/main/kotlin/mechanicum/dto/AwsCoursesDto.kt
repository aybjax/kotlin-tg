package mechanicum.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import mechanicum.dto.aws_course.CourseDto

@JsonClass(generateAdapter = true)
data class AwsCoursesDto(
    @Json(name="Courses")
    val cours: List<CourseDto>
)
