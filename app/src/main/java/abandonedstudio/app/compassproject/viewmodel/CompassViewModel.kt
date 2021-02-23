package abandonedstudio.app.compassproject.viewmodel

import abandonedstudio.app.compassproject.model.DestinationRepository
import abandonedstudio.app.compassproject.util.Tracking
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CompassViewModel @Inject constructor(private val repository: DestinationRepository) : ViewModel() {

    val distanceFromDestination = MutableLiveData<Int>()
    val bearingToDest = MutableLiveData<Float>()

    var isTracking = false

    fun getDestinationLatitude(): Float{
        return repository.getDestinationLatitude()
    }

    fun getDestinationLongitude(): Float {
        return repository.getDestinationLongitude()
    }

    fun getDestinationLocation(): Location{
        return Location("destination location").apply {
            latitude = getDestinationLatitude().toDouble()
            longitude = getDestinationLongitude().toDouble()
        }
    }

//    private var trackPositionJob: Job? = null

    fun setDestination(latitude: Float, longitude: Float){
        repository.setDestinationCoordinates(latitude, longitude)
    }

    fun currentLocationUpdated(currentLocation: Location){
        Log.d("lista", "poszla")
        distanceFromDestination.postValue(Tracking.calculateDistance(currentLocation, getDestinationLocation()))
        bearingToDest.postValue(Tracking.calculateBearing(currentLocation, getDestinationLocation()))
    }

//    private fun trackJob(): Job{
//        return viewModelScope.launch {
////            track
////            checking location with 1 sec period
//            delay(1000)
//        }
//    }
//
//    fun startTracking(){
//        trackPositionJob = trackJob()
//    }
//
//    fun stopTracking(){
//        trackPositionJob?.cancel()
//    }

}