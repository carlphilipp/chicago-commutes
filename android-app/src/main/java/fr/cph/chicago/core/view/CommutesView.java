package fr.cph.chicago.core.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import fr.cph.chicago.R;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.LayoutUtil;
import fr.cph.chicago.util.Util;

/**
 * @author carl
 */
public class CommutesView {

    @NonNull
    public static LinearLayout createBikeFirstLine(@NonNull final Context context, @NonNull final BikeStation bikeStation) {
        return createBikeLine(context, bikeStation, true);
    }

    @NonNull
    public static LinearLayout createBikeSecondLine(@NonNull final Context context, @NonNull final BikeStation bikeStation) {
        return createBikeLine(context, bikeStation, false);
    }

    @NonNull
    private static LinearLayout createBikeLine(@NonNull final Context context, @NonNull final BikeStation bikeStation, final boolean firstLine) {
        int pixels = Util.convertDpToPixel(context, 16);
        int pixelsHalf = pixels / 2;
        int grey5 = ContextCompat.getColor(context, R.color.grey_5);


        final LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final LinearLayout line = new LinearLayout(context);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setLayoutParams(lineParams);

        // Left
        final LinearLayout.LayoutParams leftParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final RelativeLayout left = new RelativeLayout(context);
        left.setLayoutParams(leftParam);

        final RelativeLayout lineIndication = LayoutUtil.createColoredRoundForFavorites(context, TrainLine.NA);
        int lineId = Util.generateViewId();
        lineIndication.setId(lineId);

        final RelativeLayout.LayoutParams availableParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        availableParam.addRule(RelativeLayout.RIGHT_OF, lineId);
        availableParam.setMargins(pixelsHalf, 0, 0, 0);

        final TextView boundCustomTextView = new TextView(context);
        boundCustomTextView.setText(context.getString(R.string.bike_available_docks));
        boundCustomTextView.setSingleLine(true);
        boundCustomTextView.setLayoutParams(availableParam);
        boundCustomTextView.setTextColor(grey5);
        int availableId = Util.generateViewId();
        boundCustomTextView.setId(availableId);

        final RelativeLayout.LayoutParams availableValueParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        availableValueParam.addRule(RelativeLayout.RIGHT_OF, availableId);
        availableValueParam.setMargins(pixelsHalf, 0, 0, 0);

        final TextView amountBike = new TextView(context);
        final String text = firstLine ? context.getString(R.string.bike_available_bikes) : context.getString(R.string.bike_available_docks);
        boundCustomTextView.setText(text);
        final Integer data = firstLine ? bikeStation.getAvailableBikes() : bikeStation.getAvailableDocks();
        if (data == null) {
            amountBike.setText("?");
            amountBike.setTextColor(ContextCompat.getColor(context, R.color.orange));
        } else {
            amountBike.setText(String.valueOf(data));
            final int color = data == 0 ? R.color.red : R.color.green;
            amountBike.setTextColor(ContextCompat.getColor(context, color));
        }
        amountBike.setLayoutParams(availableValueParam);

        left.addView(lineIndication);
        left.addView(boundCustomTextView);
        left.addView(amountBike);
        line.addView(left);
        return line;
    }
}
