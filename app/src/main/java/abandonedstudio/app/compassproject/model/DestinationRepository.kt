package abandonedstudio.app.compassproject.model

import android.location.Location
import javax.inject.Inject

class DestinationRepository @Inject constructor(private val destination: Destination){

    fun getDestinationLatitude(): Double {
        return destination.destinationLatitude
    }

    fun getDestinationLongitude(): Double {
        return destination.destinationLongitude
    }

    fun  getDestination(): Location{
        return Location("destination location").apply {
            latitude = destination.destinationLatitude
            longitude = destination.destinationLongitude
        }
    }

    fun setDestinationCoordinates(latitude: Double, longitude: Double){
        destination.destinationLatitude = latitude
        destination.destinationLongitude = longitude
    }

}