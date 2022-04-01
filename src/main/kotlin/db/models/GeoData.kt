package db.models

import dataclasses.geocoding.Latlong
import org.jetbrains.exposed.dao.id.IntIdTable

package db.models

/**
 * Users table represented with Exposed library
 */
object GeoData: IntIdTable() {
    val city = varchar("city", length = 50)
    val state = varchar("state", length = 50)
    val country = varchar("country", length = 50)
    val country_code = varchar("country_code", length = 50)
    val latitude = double("latitude").index()
    val longitude = double("longitude").index()
}

/**
 * User Dao
 */
class GeoDataDao(id: EntityID<Int>): IntEntity(id)
{
    var city by GeoData.city
    var state by GeoData.state
    var country by GeoData.country
    var country_code by GeoData.country_code
    var latitude by GeoData.latitude
    var longitude by GeoData.longitude

    var latlong: Latlong
        get() = Latlong(latitude, longitude)
        set(value) {
            latitude = value.latitude
            longitude = value.longitude
        }
}