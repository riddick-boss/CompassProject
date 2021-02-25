package abandonedstudio.app.compassproject.util

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

object PermissionsManager {

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

//    @RequiresApi(Build.VERSION_CODES.M)
//    fun createLocationPermDialog(context: Context): AlertDialog? {
//        return AlertDialog.Builder(context).apply {
//            setTitle("Permission required")
//            setMessage("Permission needed to calculate distance")
//            setPositiveButton("OK"){ _, _ ->
////                activity.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), Constants.FINE_LOCATION_RQ)
//            }
//        }.create()
//    }

    fun askForLocationAccess(activity: Activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), Constants.FINE_LOCATION_RQ)
//            if (activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
//                return false
//                AlertDialog.Builder(activity.applicationContext).apply {
//                    setTitle("Permission required")
//                    setMessage("Permission needed to calculate distance")
//                    setPositiveButton("OK"){ _, _ ->
//                        activity.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), Constants.FINE_LOCATION_RQ)
//                    }
//                }.create()
//                        .show()
//            } else{
//                activity.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), Constants.FINE_LOCATION_RQ)
//            }
        }
//        return true
    }
}