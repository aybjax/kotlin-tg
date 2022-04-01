package dataclasses.geocoding

data class CoordinateBox(
    val topLeft: Double,
    val topRight: Double,
    val bottomRight: Double,
    val bottomLeft: Double,
)
