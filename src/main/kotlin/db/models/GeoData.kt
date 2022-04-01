package db.models

import dataclasses.geocoding.Latlong
import dataclasses.geocoding.geocoding_response.Property
import extensions.kmToDegree
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.properties.Delegates

/**
 * Users table represented with Exposed library
 */
object GeoData: IntIdTable() {
    val city = varchar("city", length = 50).nullable()
    val state = varchar("state", length = 50).nullable()
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
    val location: String
        get() = buildString {
            this@GeoDataDao.city?.let {
                append(this@GeoDataDao.city)
            }

            this@GeoDataDao.state?.let {
                append(" ")
                append(this@GeoDataDao.state)
            }

            append(" ")
            append(this@GeoDataDao.country)

            append(" ")
            append("(${this@GeoDataDao.country_code})")
        }

    infix fun distanceTo(latlong: Latlong): Double
    {
        return Math.sqrt(
            (latitude - latlong.latitude) * (latitude - latlong.latitude) +
                    (longitude - latlong.longitude) * (longitude - latlong.longitude)
        )
    }

    companion object: IntEntityClass<GeoDataDao>(GeoData) {
        var RADIUS: Double = (5.0).kmToDegree()

        fun getLocation(latlong: Latlong): String?
        {
            val left = latlong.longitude - RADIUS
            val right = latlong.longitude + RADIUS
            val top = latlong.latitude - RADIUS
            val bottom = latlong.latitude + RADIUS

            val datas = transaction {
                GeoDataDao.find {
                    (GeoData.longitude greater left) and
                            (GeoData.longitude less right) and
                            (GeoData.latitude greater top) and
                            (GeoData.latitude less bottom)
                }.toList()
            }

            if(datas.isEmpty()) return null

            var distance  = -1.0
            var datum by Delegates.notNull<GeoDataDao>()

            datas.forEach { geodata ->
                val d = geodata distanceTo latlong
                if(d > distance) {
                    distance = d
                    datum = geodata
                }
            }

            return datum.location
        }

        fun setLocation(latlong: Latlong, property: Property): GeoDataDao {
            return transaction {
                GeoDataDao.new {
                    city = property.city ?: ""
                    state = property.state ?: ""
                    country = property.country ?: ""
                    country_code = property.country_code ?: ""
                    latitude = latlong.latitude ?: 0.0
                    longitude = latlong.longitude ?: 0.0
                }
            }
        }
    }
}