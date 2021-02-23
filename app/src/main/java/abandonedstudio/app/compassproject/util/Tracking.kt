package abandonedstudio.app.compassproject.util

import android.location.Location

object Tracking {

    fun calculateDistance(currentLocation: Location, destination: Location): Int {
        return currentLocation.distanceTo(destination).toInt()
    }

    fun calculateBearing(currentLocation: Location, destination: Location): Float{
        return currentLocation.bearingTo(destination)
    }

}