package fr.cph.chicago.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import com.annimon.stream.Stream;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

abstract class RefreshMarkers {

    @Getter(AccessLevel.PROTECTED)
    private final BitmapDescriptor bitmapDescrSmall;
    @Getter(AccessLevel.PROTECTED)
    private final BitmapDescriptor bitmapDescrMedium;
    @Getter(AccessLevel.PROTECTED)
    private final BitmapDescriptor bitmapDescrLarge;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private BitmapDescriptor currentDescriptor;

    RefreshMarkers(@NonNull final Context context, @DrawableRes int drawable) {
        final Bitmap icon = BitmapFactory.decodeResource(context.getResources(), drawable);
        bitmapDescrSmall = createBitMapDescriptor(icon, 9);
        bitmapDescrMedium = createBitMapDescriptor(icon, 5);
        bitmapDescrLarge = createBitMapDescriptor(icon, 3);
        currentDescriptor = bitmapDescrSmall;
    }

    private BitmapDescriptor createBitMapDescriptor(final Bitmap icon, final int size) {
        final Bitmap bitmap = Bitmap.createScaledBitmap(icon, icon.getWidth() / size, icon.getHeight() / size, true);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    boolean isIn(final float num, final float sup, final float inf) {
        return num >= inf && num <= sup;
    }

    public void refresh(@NonNull final CameraPosition position, @NonNull final List<Marker> markers) {
        float currentZoom = -1;
        if (position.zoom != currentZoom) {
            float oldZoom = currentZoom;
            currentZoom = position.zoom;

            if (isIn(currentZoom, 12.9f, 11f) && !isIn(oldZoom, 12.9f, 11f)) {
                Stream.of(markers).forEach(marker -> marker.setIcon(getBitmapDescrSmall()));
                setCurrentDescriptor(getBitmapDescrSmall());
            } else if (isIn(currentZoom, 14.9f, 13f) && !isIn(oldZoom, 14.9f, 13f)) {
                Stream.of(markers).forEach(marker -> marker.setIcon(getBitmapDescrMedium()));
                setCurrentDescriptor(getBitmapDescrMedium());
            } else if (isIn(currentZoom, 21f, 15f) && !isIn(oldZoom, 21f, 15f)) {
                Stream.of(markers).forEach(marker -> marker.setIcon(getBitmapDescrLarge()));
                setCurrentDescriptor(getBitmapDescrLarge());
            }
        }
    }
}
