package models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TestData(
    val id: Int,
    val title: String,
    val description: String,
)