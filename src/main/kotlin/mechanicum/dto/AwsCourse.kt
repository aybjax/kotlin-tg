package mechanicum.dto

import com.squareup.moshi.Json
import mechanicum.dto.aws_course.Course
import mechanicum.dto.aws_course.Process

data class AwsCourse(
    @Json(name = "Courses")
    val course: Course,
    @Json(name = "Processes")
    val processes: List<Process>
)
