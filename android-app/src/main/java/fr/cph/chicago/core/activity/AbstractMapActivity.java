package fr.cph.chicago.core.activity;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

import butterknife.BindDrawable;
import butterknife.BindView;
import fr.cph.chicago.R;
import fr.cph.chicago.util.Util;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static fr.cph.chicago.Constants.GPS_ACCESS;

@SuppressWarnings("WeakerAccess")
@SuppressLint("Registered")
public class AbstractMapActivity extends FragmentActivity implements EasyPermissions.PermissionCallbacks, GoogleMap.OnCameraIdleListener, OnMapReadyCallback {

    @BindView(android.R.id.content)
    ViewGroup viewGroup;
    @BindView(R.id.map_container)
    LinearLayout layout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindDrawable(R.drawable.ic_arrow_back_white_24dp)
    Drawable arrowBackWhite;

    protected final Util util;

    private Marker selectedMarker;
    private GoogleMap googleMap;

    boolean refreshingInfoWindow = false;

    public AbstractMapActivity() {
        util = Util.INSTANCE;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void initData() {
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    protected void setToolbar() {
        toolbar.inflateMenu(R.menu.main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(4);
        }

        toolbar.setNavigationIcon(arrowBackWhite);
        toolbar.setOnClickListener(v -> finish());
    }

    public void refreshInfoWindow() {
        refreshingInfoWindow = true;
        selectedMarker.showInfoWindow();
        refreshingInfoWindow = false;
    }

    protected void centerMapOn(final double latitude, final double longitude, final int zoom) {
        final LatLng latLng = new LatLng(latitude, longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(GPS_ACCESS)
    private void enableMyLocationOnMapIfAllowed() {
        if (EasyPermissions.hasPermissions(getApplicationContext(), ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) {
            setLocationOnMap();
        } else {
            EasyPermissions.requestPermissions(this, "Would you like to see your current location on the map?", GPS_ACCESS, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION);
        }
    }

    @Override
    public void onCameraIdle() {

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setOnCameraIdleListener(this);
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(util.getChicago(), 10));
        enableMyLocationOnMapIfAllowed();
    }

    @Override
    public final void onPermissionsGranted(int requestCode, List<String> perms) {
        setLocationOnMap();
    }

    @Override
    public final void onPermissionsDenied(int requestCode, List<String> perms) {
    }

    private void setLocationOnMap() throws SecurityException {
        googleMap.setMyLocationEnabled(true);
    }

    public void setSelectedMarker(Marker selectedMarker) {
        this.selectedMarker = selectedMarker;
    }

    public GoogleMap getGoogleMap() {
        return googleMap;
    }
}
