package mechanicum.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import mechanicum.dto.aws_course.ProcessDto

/**
 * AWS DTO for id.json files
 */
@JsonClass(generateAdapter = true)
data class AwsCourseDto(
    @Json(name = "Processes")
    val processDtos: List<ProcessDto>
)
