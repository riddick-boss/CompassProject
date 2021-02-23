package abandonedstudio.app.compassproject.model

import android.util.Log
import javax.inject.Inject

class DestinationRepository @Inject constructor(private val destination: Destination){

    fun getDestinationLatitude(): Float {
        Log.d("lista", "${destination.destinationLatitude} up")
        return destination.destinationLatitude
    }

    fun getDestinationLongitude(): Float {
        return destination.destinationLongitude
    }

    fun setDestinationCoordinates(latitude: Float, longitude: Float){
//        destination = Destination(latitude, longitude)
        destination.destinationLatitude = latitude
        destination.destinationLongitude = longitude
        Log.d("lista", "${destination.destinationLatitude} set")
    }

}