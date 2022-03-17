package mechanicum.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import mechanicum.dto.aws_course.CourseDto

/**
 * AWS Course DTO for Courses.json
 */
@JsonClass(generateAdapter = true)
data class AwsCoursesDto(
    @Json(name="Courses")
    val course: List<CourseDto>
)
