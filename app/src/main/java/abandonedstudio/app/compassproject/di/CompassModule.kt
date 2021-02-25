package abandonedstudio.app.compassproject.di

import abandonedstudio.app.compassproject.util.DestinationDialogFragment
import android.annotation.SuppressLint
import android.content.Context
import android.hardware.SensorManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.FragmentScoped


@Module
@InstallIn(FragmentComponent::class)
object CompassModule {

    @SuppressLint("VisibleForTests")
    @FragmentScoped
    @Provides
    fun provideFusedLocationProviderClient(
            @ApplicationContext context: Context
    ) = FusedLocationProviderClient(context)

    @FragmentScoped
    @Provides
    fun provideLocationRequest(): LocationRequest = LocationRequest.create().apply {
        interval = 2000
        fastestInterval = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    @FragmentScoped
    @Provides
    fun provideSensorManager(
            @ApplicationContext context: Context
    ) = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    @FragmentScoped
    @Provides
    fun provideDestinationDialog() = DestinationDialogFragment()

}