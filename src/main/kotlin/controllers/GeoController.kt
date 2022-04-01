package controllers

import dataclasses.geocoding.Latlong
import dataclasses.request.Request
import db.models.GeoDataDao
import geocoding.Geoapify
import org.jetbrains.exposed.sql.transactions.transaction

object GeoController {
    suspend fun getLocation(request: Request, latlong: Latlong): String
    {
        GeoDataDao.getLocation(latlong)?.let {
            return it
        }

        val geocoding = Geoapify.getLocation(latlong) ?: return ""

        val property = geocoding.features.getOrNull(0)?.property ?: return ""

        GeoDataDao.setLocation(latlong, property)

        if(property.location.isNotEmpty()) {
            request.user.updateCompletion {
                it.location = property.location
                it.latlong = latlong

                it
            }
        }

        return property.location
    }
}
