/**
 * Copyright 2021 Carl-Philipp Harmant
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.core.fragment

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import fr.cph.chicago.Constants.GPS_ACCESS
import fr.cph.chicago.R
import fr.cph.chicago.core.adapter.SlidingUpAdapter
import fr.cph.chicago.core.listener.OnMarkerClickListener
import fr.cph.chicago.core.model.BikeStation
import fr.cph.chicago.core.model.BusStop
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.TrainStation
import fr.cph.chicago.core.model.marker.MarkerDataHolder
import fr.cph.chicago.databinding.FragmentNearbyBinding
import fr.cph.chicago.redux.store
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.GoogleMapUtil
import fr.cph.chicago.util.GoogleMapUtil.getBitmapDescriptor
import fr.cph.chicago.util.MapUtil
import fr.cph.chicago.util.MapUtil.chicagoPosition
import fr.cph.chicago.util.Util
import io.reactivex.rxjava3.core.Single
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

/**
 * Nearby Fragment
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class NearbyFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    companion object {
        private val util: Util = Util
        private val googleMapUtil = GoogleMapUtil
        private val mapUtil = MapUtil
        private val trainService = TrainService
        private val busService = BusService

        fun newInstance(sectionNumber: Int): NearbyFragment {
            return fragmentWithBundle(NearbyFragment(), sectionNumber) as NearbyFragment
        }
    }
    private var _binding: FragmentNearbyBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapFragment: SupportMapFragment
    lateinit var slidingUpAdapter: SlidingUpAdapter
    lateinit var loadingLayout: LinearLayout
    lateinit var slidingLayoutPanel: SlidingUpPanelLayout

    private lateinit var markerDataHolder: MarkerDataHolder
    private var fusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentNearbyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadingLayout = binding.loadingLayoutContainer
        slidingLayoutPanel = binding.slidingLayout
        slidingUpAdapter = SlidingUpAdapter(this)
        markerDataHolder = MarkerDataHolder()
        binding.searchAreaButton.setOnClickListener { view ->
            view.visibility = View.INVISIBLE
            mapFragment.getMapAsync { googleMap ->
                googleMap.clear()
                markerDataHolder.clear()
                val target = googleMap.cameraPosition.target
                handleNearbyData(Position(target.latitude, target.longitude), false)
            }
        }
        showProgress(true)
    }

    override fun onStart() {
        super.onStart()
        val options = GoogleMapOptions()
        val camera = CameraPosition(GoogleMapUtil.chicago, 7f, 0f, 0f)
        options.camera(camera)
        mapFragment = SupportMapFragment.newInstance(options)
        mapFragment.retainInstance = true
        val fm = activity!!.supportFragmentManager
        loadNearbyIfAllowed()
        fm.beginTransaction().replace(R.id.map, mapFragment).commit()
    }

    override fun onResume() {
        super.onResume()
        slidingLayoutPanel.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
    }

    private fun updateMarkersAndModel(
        busStops: List<BusStop>,
        trainTrainStation: List<TrainStation>,
        bikeStations: List<BikeStation>) {
        if (isAdded) {
            mapFragment.getMapAsync { googleMap ->
                with(googleMap) {
                    uiSettings.isMyLocationButtonEnabled = true
                    uiSettings.isZoomControlsEnabled = false
                    uiSettings.isMapToolbarEnabled = false
                }

                val bitmapDescriptorBus = getBitmapDescriptor(context, R.drawable.bus_stop_icon)
                val bitmapDescriptorTrain = getBitmapDescriptor(context, R.drawable.train_station_icon)
                val bitmapDescriptorBike = getBitmapDescriptor(context, R.drawable.bike_station_icon)

                busStops
                    .forEach { busStop ->
                        val point = LatLng(busStop.position.latitude, busStop.position.longitude)
                        val markerOptions = MarkerOptions()
                            .position(point)
                            .title(busStop.name)
                            .icon(bitmapDescriptorBus)
                        val marker = googleMap.addMarker(markerOptions)
                        marker?.run {
                            markerDataHolder.addData(marker, busStop)
                        }
                    }

                trainTrainStation
                    .forEach { station ->
                        val position = station.stopsPosition.firstOrNull()
                        if (position != null) {
                            val markerOptions = MarkerOptions()
                                .position(LatLng(position.latitude, position.longitude))
                                .title(station.name)
                                .icon(bitmapDescriptorTrain)
                            val marker = googleMap.addMarker(markerOptions)
                            marker?.run {
                                markerDataHolder.addData(marker, station)
                            }
                        }
                    }

                bikeStations
                    .forEach { station ->
                        val markerOptions = MarkerOptions()
                            .position(LatLng(station.latitude, station.longitude))
                            .title(station.name)
                            .icon(bitmapDescriptorBike)
                        val marker = googleMap.addMarker(markerOptions)
                        marker?.run {
                            markerDataHolder.addData(marker, station)
                        }
                    }

                showProgress(false)
                googleMap.setOnMarkerClickListener(OnMarkerClickListener(markerDataHolder, this@NearbyFragment))
                googleMap.setOnCameraMoveListener { binding.searchAreaButton.visibility = View.VISIBLE }
            }
        }
    }

    fun showProgress(show: Boolean) {
        if (isAdded) {
            if (show) {
                binding.progressBar.visibility = View.VISIBLE
                binding.progressBar.progress = 50
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    @AfterPermissionGranted(GPS_ACCESS)
    private fun loadNearbyIfAllowed() {
        if (EasyPermissions.hasPermissions(context!!, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) {
            startLoadingNearby()
        } else {
            EasyPermissions.requestPermissions(this, "To access that feature, we need to access your current location", GPS_ACCESS, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        startLoadingNearby()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        showProgress(false)
    }

    @SuppressLint("MissingPermission")
    private fun startLoadingNearby() {
        showProgress(true)
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        }
        fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
            val position = if (location == null)
                Position()
            else
                Position(location.latitude, location.longitude)
            handleNearbyData(position, true)
        }
    }

    @SuppressLint("CheckResult")
    private fun handleNearbyData(position: Position, zoomIn: Boolean) {
        var finalPosition = position
        if (position.longitude == 0.0 && position.latitude == 0.0) {
            Timber.w("Could not get current user location")
            util.showSnackBar(binding.loadingLayoutContainer, R.string.message_cant_find_location)
            finalPosition = chicagoPosition
        }

        val trainStationAround = trainService.readNearbyStation(finalPosition)
        val busStopsAround = busService.busStopsAround(finalPosition)
        val bikeStationsAround = mapUtil.readNearbyStation(finalPosition, store.state.bikeStations)
        Single.zip(trainStationAround, busStopsAround, bikeStationsAround, { trains, buses, bikeStations ->
            googleMapUtil.centerMap(mapFragment, zoomIn, finalPosition)
            updateMarkersAndModel(buses, trains, bikeStations)
            Any()
        }).subscribe({}, { error -> Timber.e(error) })
    }
}
