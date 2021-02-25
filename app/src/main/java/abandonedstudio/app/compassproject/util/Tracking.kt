package abandonedstudio.app.compassproject.util

import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.core.location.LocationManagerCompat

object Tracking {

    fun calculateDistance(currentLocation: Location, destination: Location): Int {
        return currentLocation.distanceTo(destination).toInt()
    }

    fun calculateBearing(currentLocation: Location, destination: Location): Float{
        return currentLocation.bearingTo(destination)
    }

//    check if location is turned on
    fun isLocationEnabled(context: Context): Boolean{
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

}