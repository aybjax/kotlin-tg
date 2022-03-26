package dataclasses.aws

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * AWS Course DTO for Courses.kt.json
 */
@JsonClass(generateAdapter = true)
data class AwsCoursesDto(
    @Json(name="Courses")
    val course: List<AwsCourseDto>
)
