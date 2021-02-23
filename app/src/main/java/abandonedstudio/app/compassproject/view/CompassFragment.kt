package abandonedstudio.app.compassproject.view

import abandonedstudio.app.compassproject.R
import abandonedstudio.app.compassproject.databinding.CompassFragmentBinding
import abandonedstudio.app.compassproject.util.Animations
import abandonedstudio.app.compassproject.util.Constants.FINE_LOCATION_RQ
import abandonedstudio.app.compassproject.util.DestinationDialogFragment
import abandonedstudio.app.compassproject.util.PermissionsManager
import abandonedstudio.app.compassproject.viewmodel.CompassViewModel
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationRequest.create
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CompassFragment: Fragment(), DestinationDialogFragment.OnSetDestinationListener, SensorEventListener {

    private val viewModel: CompassViewModel by viewModels()

    private var _binding: CompassFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var destinationDialog: DestinationDialogFragment

    private var isTrackingNow = false

    private var currentCompassDegree = 0f
    private lateinit var sensorManager:SensorManager

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        destinationDialog = DestinationDialogFragment()
        destinationDialog.setOnSetDestinationListener(this)

        binding.setDestinationButton.setOnClickListener {
            if (checkForLocationAccess()){
                if(isLocationEnabled(requireContext())){
                    toggleTracking()
                } else{
                    Snackbar.make(requireView(), "Turn on location", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        subscribeToObservers()
    }

    override fun onResume() {
        super.onResume()

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
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        stopUpdatingLocation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun subscribeToObservers(){
        viewModel.distanceFromDestination.observe(viewLifecycleOwner, {
            binding.distanceTextView.text = "$it m"
        })

        viewModel.bearingToDest.observe(viewLifecycleOwner, {
//            TODO: animate compass idle
        })
    }

    private fun toggleTracking(){
        if (isTrackingNow){
            stopUpdatingLocation()
            viewModel.isTracking = false
            binding.setDestinationButton.text = getString(R.string.set_destination)
            isTrackingNow = !isTrackingNow
        } else{
            isTrackingNow = !isTrackingNow
            showDestinationDialog()
        }
    }

    private fun showDestinationDialog(){
        destinationDialog.show(parentFragmentManager, "SetDestinationDialog")
    }

//    already checked for nullability in dialog (DestinationDialogFragment)
    override fun onCoordinatesEntered(latitude: Float, longitude: Float) {
        binding.setDestinationButton.text = getString(R.string.stop_tracking)
        viewModel.setDestination(latitude, longitude)
        Log.d("lista", "ui")
        binding.latTextView.text = viewModel.getDestinationLatitude().toString()
        binding.lngTextView.text = viewModel.getDestinationLongitude().toString()
        getLocationUpdates()
        startUpdatingLocation()
    }

//    asking for location access
    private fun checkForLocationAccess(): Boolean{
        if (PermissionsManager.checkLocationPermissions(requireContext())){
            return true
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                when{
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> showLocationDialog()

                    else -> activity?.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINE_LOCATION_RQ)
                }
            }
        }
        return false
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
        if (!PermissionsManager.checkLocationPermissions(requireContext())){
            Toast.makeText(requireContext(), "Cannot access location", Toast.LENGTH_SHORT).show()
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

        binding.compassImageView.startAnimation(
                Animations.animateCompassRotation(currentCompassDegree, -azimuthDeg)
        )
        currentCompassDegree = -azimuthDeg
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
//        no-op
    }

//    check if location is turned on
    private fun isLocationEnabled(context: Context): Boolean{
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    private fun getLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = create().apply {
            interval = 5000
            fastestInterval = 4000
            priority = PRIORITY_HIGH_ACCURACY
        }
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
    }

    private fun stopUpdatingLocation() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


}