package abandonedstudio.app.compassproject.view

import abandonedstudio.app.compassproject.R
import abandonedstudio.app.compassproject.databinding.CompassFragmentBinding
import abandonedstudio.app.compassproject.util.Animations
import abandonedstudio.app.compassproject.util.Constants.FINE_LOCATION_RQ
import abandonedstudio.app.compassproject.util.DestinationDialogFragment
import abandonedstudio.app.compassproject.util.PermissionsManager
import abandonedstudio.app.compassproject.util.Tracking
import abandonedstudio.app.compassproject.viewmodel.CompassViewModel
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.absoluteValue

@AndroidEntryPoint
class CompassFragment: Fragment(), DestinationDialogFragment.OnSetDestinationListener, SensorEventListener {

    private val viewModel: CompassViewModel by viewModels()

    private var _binding: CompassFragmentBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var destinationDialog: DestinationDialogFragment

    private var currentCompassDegree = 0f
    private var currentIndicatorDegree = 0f
    @Inject
    lateinit var sensorManager: SensorManager

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient
    @Inject
    lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CompassFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkOrientationAndSetCompass()

        destinationDialog.setOnSetDestinationListener(this)

        if (viewModel.isTracking){
            getLocationUpdates()
        }

        binding.setDestinationButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (PermissionsManager.hasLocationPermission(requireContext())) {
                    if (Tracking.isLocationEnabled(requireContext())) {
                        toggleTracking()
                    } else {
                        Snackbar.make(requireView(), "Turn on location", Snackbar.LENGTH_SHORT).show()
                    }
                } else {
                    showLocationDialog()
                }
            } else {
                if (Tracking.isLocationEnabled(requireContext())) {
                    toggleTracking()
                } else {
                    Snackbar.make(requireView(), "Turn on location", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        subscribeToObservers()
    }

    override fun onResume() {
        super.onResume()

//        tracking current north direction by these sensors
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    SensorManager.SENSOR_DELAY_UI
            )
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD).also {
            sensorManager.registerListener(
                    this,
                    it,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    SensorManager.SENSOR_DELAY_UI
            )
        }

        if (viewModel.isTracking){
            startUpdatingLocation()
            binding.setDestinationButton.text = getString(R.string.stop_tracking)
            setLatLngInfo()
        }
    }

    override fun onPause() {
        super.onPause()
//        stop tracking users location and north direction
        sensorManager.unregisterListener(this)
        if(viewModel.isTracking){
            stopUpdatingLocation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    private fun subscribeToObservers(){
        viewModel.distanceFromDestination.observe(viewLifecycleOwner, {
            binding.distanceTextView.text = "$it m"
        })
    }

//    checking orientation and pre-setting compass in correct orientation (north pointing to phone top)
    private fun checkOrientationAndSetCompass(){
    val orientation: Int?
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            orientation = requireContext().display?.rotation
        } else {
//            to detect orientation on api < 30
            @Suppress("DEPRECATION")
            orientation = (requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.orientation
        }
        if (orientation == Surface.ROTATION_270){
            binding.compassImageView.rotation = 90f
            binding.directionIndicatorConstraintLayout.rotation = 180f
        }
    }

//    start/stop tracking and update ui
    private fun toggleTracking(){
        if (viewModel.isTracking){
            stopUpdatingLocation()
            viewModel.isTracking = false
            binding.setDestinationButton.text = getString(R.string.set_destination)
            viewModel.bearingToDest.postValue(0f)
            viewModel.distanceFromDestination.postValue(0)
            binding.distanceTextView.text = "x m"
            binding.latTextView.text = getString(R.string.latitude)
            binding.lngTextView.text = getString(R.string.longitude)
        } else{
            showDestinationDialog()
        }
    }

    private fun setLatLngInfo(){
        binding.latTextView.text = viewModel.getDestinationLatitude().toString()
        binding.lngTextView.text = viewModel.getDestinationLongitude().toString()
    }

    private fun showDestinationDialog(){
        destinationDialog.show(parentFragmentManager, "SetDestinationDialog")
    }

//    already checked for nullability in dialog (DestinationDialogFragment)
    override fun onCoordinatesEntered(latitude: Double, longitude: Double) {
        Toast.makeText(requireContext(), "Destination set", Toast.LENGTH_SHORT).show()
        binding.setDestinationButton.text = getString(R.string.stop_tracking)
        viewModel.setDestination(latitude, longitude)
        setLatLngInfo()
        getLocationUpdates()
        startUpdatingLocation()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun showLocationDialog() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Permission required")
            setMessage("Permission needed to calculate distance")
            setPositiveButton("OK"){ _, _ ->
                activity?.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINE_LOCATION_RQ)
            }
        }.create()
                .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (PermissionsManager.hasLocationPermission(requireContext())){
            Toast.makeText(requireContext(), "Permission granted", Toast.LENGTH_SHORT).show()
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                showLocationDialog()
            }
        } else {
            Toast.makeText(requireContext(), "You have to enable location permission in settings", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
            }
        }

        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        val azimuthDeg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        val indicatorDeg = viewModel.bearingToDest.value ?: 0f

        if ((azimuthDeg-currentCompassDegree).absoluteValue > 10 || (indicatorDeg-currentIndicatorDegree).absoluteValue > 10){
            binding.compassImageView.startAnimation(
                    Animations.animateCompassRotation(currentCompassDegree, -azimuthDeg)
            )
            binding.directionIndicatorConstraintLayout.startAnimation(
                    Animations.animateCompassRotation(currentIndicatorDegree, -azimuthDeg + indicatorDeg)
            )
            currentCompassDegree = -azimuthDeg
            currentIndicatorDegree = -azimuthDeg + indicatorDeg
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) { /* no-op */ }

//    tracking current user's location and updating direction and distance to destination
    private fun getLocationUpdates() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                if (locationResult.locations.isNotEmpty()) {
                    val location = locationResult.lastLocation
                    viewModel.currentLocationUpdated(location)
                }
            }
        }
        viewModel.isTracking = true
    }

    @SuppressLint("MissingPermission")
    private fun startUpdatingLocation() {
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        )
        binding.directionIndicatorImageView.visibility = View.VISIBLE
    }

    private fun stopUpdatingLocation() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        binding.directionIndicatorImageView.visibility = View.INVISIBLE
    }

}