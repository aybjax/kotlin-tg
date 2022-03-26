package dataclasses.aws

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * AWS DTO for id.json files
 */
@JsonClass(generateAdapter = true)
data class AwsProcessesDto(
    @Json(name = "Processes")
    val awsProcessDtos: List<AwsProcessDto>
)
