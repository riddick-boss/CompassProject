package abandonedstudio.app.compassproject.viewmodel

import abandonedstudio.app.compassproject.model.DestinationRepository
import abandonedstudio.app.compassproject.util.Tracking
import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CompassViewModel @Inject constructor(private val repository: DestinationRepository) : ViewModel() {

    val distanceFromDestination = MutableLiveData<Int>()
    val bearingToDest = MutableLiveData<Float>()

    var isTracking = false

    fun getDestinationLatitude(): Double{
        return repository.getDestinationLatitude()
    }

    fun getDestinationLongitude(): Double {
        return repository.getDestinationLongitude()
    }

    private fun getDestinationLocation(): Location{
        return repository.getDestination()
    }

    fun setDestination(latitude: Double, longitude: Double){
        repository.setDestinationCoordinates(latitude, longitude)
    }

    fun currentLocationUpdated(currentLocation: Location){
        distanceFromDestination.postValue(Tracking.calculateDistance(currentLocation, getDestinationLocation()))
        bearingToDest.postValue(Tracking.calculateBearing(currentLocation, getDestinationLocation()))
    }

}