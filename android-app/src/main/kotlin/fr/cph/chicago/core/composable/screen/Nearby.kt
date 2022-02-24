package fr.cph.chicago.core.composable.screen

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cph.chicago.R
import fr.cph.chicago.core.composable.MainViewModel
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.toLatLng
import fr.cph.chicago.util.GoogleMapUtil
import fr.cph.chicago.util.MapUtil.chicagoPosition
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Nearby(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel
) {
    var isMapLoaded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val locationViewModel = LocationViewModel()

    PermissionUI(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
        "Location rational",
        mainViewModel.uiState.snackbarHostState
    ) { permissionAction ->

        when (permissionAction) {
            is PermissionAction.OnPermissionGranted -> {
                Timber.d("Permission grant successful")
                locationViewModel.setRequestLocationPermission(false)
                //To Do: Request User Location

                val locationRequest = LocationRequest.create().apply {
                    interval = 1000
                    fastestInterval = 1000
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }

                val client: SettingsClient = LocationServices.getSettingsClient(context)
                val builder: LocationSettingsRequest.Builder = LocationSettingsRequest
                    .Builder()
                    .addLocationRequest(locationRequest)

                val gpsSettingTask: Task<LocationSettingsResponse> =
                    client.checkLocationSettings(builder.build())

                gpsSettingTask.addOnSuccessListener { locationSettingsResponse ->
                    val settingsStates = locationSettingsResponse.locationSettingsStates
                    if (settingsStates!!.isLocationPresent && settingsStates.isLocationUsable) {
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            val p = if (location == null)
                                Position()
                            else
                                Position(location.latitude, location.longitude)
                            mainViewModel.setCurrentUserLocation(p)
                            mainViewModel.loadNearbyStations(p)
                        }
                    } else {
                        // Handle error
                        Toast.makeText(context, "Location not available", Toast.LENGTH_LONG).show()
                        mainViewModel.setCurrentUserLocation(chicagoPosition)
                        mainViewModel.loadNearbyStations(chicagoPosition)
                    }
                }
                gpsSettingTask.addOnFailureListener { exception ->
                    if (exception is ResolvableApiException) {
                        try {
                            val intentSenderRequest = IntentSenderRequest
                                .Builder(exception.resolution)
                                .build()
                            Timber.i(exception, "Something failed")
                            //onDisabled(intentSenderRequest)
                        } catch (sendEx: IntentSender.SendIntentException) {
                            // ignore here
                        }
                    }
                }
            }
            is PermissionAction.OnPermissionDenied -> {
                Timber.d("Permission grant denied")
                locationViewModel.setRequestLocationPermission(false)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxWidth(),
        snackbarHost = { SnackbarHost(hostState = mainViewModel.uiState.snackbarHostState) { data -> Snackbar(snackbarData = data) } },
        content = {
            GoogleMapView(
                onMapLoaded = {
                    isMapLoaded = true
                },
                trainStations = mainViewModel.uiState.nearbyTrainStations,
                busStops = mainViewModel.uiState.nearbyBusStops,
                bikeStations = mainViewModel.uiState.nearbyBikeStations,
                mapPosition = mainViewModel.uiState.nearbyUserCurrentLocation,
                zoomIn = mainViewModel.uiState.nearbyZoomIn
            )
            if (!isMapLoaded) {
                AnimatedVisibility(
                    modifier = Modifier.fillMaxSize(),
                    visible = !isMapLoaded,
                    enter = EnterTransition.None,
                    exit = fadeOut()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .background(MaterialTheme.colors.background)
                            .wrapContentSize()
                    )
                }
            }
        }
    )
}

@HiltViewModel
class LocationViewModel @Inject constructor() : ViewModel() {
    private val _requestLocationPermission: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val requestLocationPermission = _requestLocationPermission.asStateFlow()

    fun setRequestLocationPermission(request: Boolean) {
        _requestLocationPermission.value = request
    }
}

sealed class PermissionAction {
    object OnPermissionGranted : PermissionAction()
    object OnPermissionDenied : PermissionAction()
}

@Composable
fun PermissionUI(
    context: Context,
    permission: String,
    permissionRationale: String,
    snackbarHostState: SnackbarHostState,
    permissionAction: (PermissionAction) -> Unit
) {
    val permissionGranted =
        GoogleMapUtil.checkIfPermissionGranted(
            context,
            permission
        )

    if (permissionGranted) {
        Timber.d("Permission already granted, exiting..")
        permissionAction(PermissionAction.OnPermissionGranted)
        return
    }


    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Timber.d("Permission provided by user")
            // Permission Accepted
            permissionAction(PermissionAction.OnPermissionGranted)
        } else {
            Timber.d("Permission denied by user")
            // Permission Denied
            permissionAction(PermissionAction.OnPermissionDenied)
        }
    }


    val showPermissionRationale = GoogleMapUtil.shouldShowPermissionRationale(
        context,
        permission
    )

    if (showPermissionRationale) {
        Timber.d("Showing permission rationale for $permission")
        LaunchedEffect(showPermissionRationale) {
            val snackbarResult = snackbarHostState.showSnackbar(
                message = permissionRationale,
                actionLabel = "Grant Access",
                duration = SnackbarDuration.Long
            )
            when (snackbarResult) {
                SnackbarResult.Dismissed -> {
                    Timber.d("User dismissed permission rationale for $permission")
                    //User denied the permission, do nothing
                    permissionAction(PermissionAction.OnPermissionDenied)
                }
                SnackbarResult.ActionPerformed -> {
                    Timber.d("User granted permission for $permission rationale. Launching permission request..")
                    launcher.launch(permission)
                }
            }
        }
    } else {
        //Request permissions again
        Timber.d("Requesting permission for $permission again")
        SideEffect {
            launcher.launch(permission)
        }
    }
}

@Composable
fun GoogleMapView(
    modifier: Modifier = Modifier,
    onMapLoaded: () -> Unit,
    trainStations: List<TrainStation>,
    busStops: List<BusStop>,
    bikeStations: List<BikeStation>,
    mapPosition: Position,
    zoomIn: Float,
) {
    Timber.i("Drawing GoogleMapView with ${trainStations.size} and  $mapPosition and ${zoomIn}")
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mapPosition.toLatLng(), zoomIn)
    }


    var mapProperties by remember { mutableStateOf(MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = true)) }
    var uiSettings by remember { mutableStateOf(MapUiSettings(compassEnabled = false, myLocationButtonEnabled = true)) }
    //var shouldAnimateZoom by remember { mutableStateOf(true) }
    //var ticker by remember { mutableStateOf(0) }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = uiSettings,
        onMapLoaded = {
            onMapLoaded()
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(mapPosition.toLatLng(), zoomIn)
            cameraPositionState.move(cameraUpdate)
        },
        googleMapOptionsFactory = {
            GoogleMapOptions().camera(CameraPosition.fromLatLngZoom(GoogleMapUtil.chicago, 12f))
        },
        onPOIClick = {
            Timber.i("POI clicked: ${it.name}")
        }
    ) {
        Timber.i("trainStationsState size: ${trainStations.size}")
        val bitmapDescriptorTrain = createStop(context, R.drawable.train_station_icon)
        val bitmapDescriptorBus = createStop(context, R.drawable.bus_stop_icon)
        val bitmapDescriptorBike = createStop(context, R.drawable.bike_station_icon)

        trainStations.forEach { trainStation ->
            Marker(
                position = trainStation.stops[0].position.toLatLng(),
                title = trainStation.name,
                icon = bitmapDescriptorTrain,
            )
        }

        busStops.forEach { busStop ->
            Marker(
                position = busStop.position.toLatLng(),
                title = busStop.name,
                icon = bitmapDescriptorBus,
            )
        }

        bikeStations.forEach { bikeStation ->
            Marker(
                position = LatLng(bikeStation.latitude, bikeStation.longitude),
                title = bikeStation.name,
                icon = bitmapDescriptorBike,
            )
        }
    }
    Column {
        DebugView(cameraPositionState)
    }
}

@Composable
private fun DebugView(cameraPositionState: CameraPositionState) {
    Column(
        Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        val moving =
            if (cameraPositionState.isMoving) "moving" else "not moving"
        Text(text = "Camera is $moving")
        Text(text = "Camera position is ${cameraPositionState.position}")
    }
}

private fun createStop(context: Context?, @DrawableRes icon: Int): BitmapDescriptor {
    return if (context != null) {
        val px = context.resources.getDimensionPixelSize(R.dimen.icon_shadow_2)
        val bitMapBusStation = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitMapBusStation)
        val shape = ContextCompat.getDrawable(context, icon)!!
        shape.setBounds(0, 0, px, bitMapBusStation.height)
        shape.draw(canvas)
        BitmapDescriptorFactory.fromBitmap(bitMapBusStation)
    } else {
        BitmapDescriptorFactory.defaultMarker()
    }
}
