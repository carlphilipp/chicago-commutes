/**
 * Copyright 2017 Carl-Philipp Harmant
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import fr.cph.chicago.R;
import fr.cph.chicago.entity.BikeStation;
import fr.cph.chicago.entity.BusArrival;
import fr.cph.chicago.entity.enumeration.BusDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Layout util class
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public enum LayoutUtil {
    ;

    // FIXME Find a way to not use context everywhere here
    @NonNull
    public static RelativeLayout createColoredRoundForFavorites(@NonNull final Context context, @NonNull final TrainLine trainLine) {
        final RelativeLayout lineIndication = new RelativeLayout(context);
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        params.height = context.getResources().getDimensionPixelSize(R.dimen.layout_round_height);
        params.width = context.getResources().getDimensionPixelSize(R.dimen.layout_round_width);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        lineIndication.setBackgroundColor(trainLine.getColor());
        lineIndication.setLayoutParams(params);
        return lineIndication;
    }

    @NonNull
    public static LinearLayout createColoredRoundForMultiple(@NonNull final Context context, @NonNull final TrainLine trainLine) {
        final LinearLayout lineIndication = new LinearLayout(context);
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        params.height = context.getResources().getDimensionPixelSize(R.dimen.layout_round_height);
        params.width = context.getResources().getDimensionPixelSize(R.dimen.layout_round_width);
        params.setMargins(10, 0, 0, 0);
        lineIndication.setBackgroundColor(trainLine.getColor());
        lineIndication.setLayoutParams(params);
        return lineIndication;
    }

    @NonNull
    public static LinearLayout.LayoutParams getInsideParams(@NonNull final Context context, final boolean newLine, final boolean lastLine) {
        int pixels = Util.convertDpToPixel(context, 16);
        int pixelsQuarter = pixels / 4;
        final LinearLayout.LayoutParams paramsLeft = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (newLine && lastLine) {
            paramsLeft.setMargins(pixels, pixelsQuarter, pixels, pixelsQuarter);
        } else if (newLine) {
            paramsLeft.setMargins(pixels, pixelsQuarter, pixels, 0);
        } else if (lastLine) {
            paramsLeft.setMargins(pixels, 0, pixels, pixelsQuarter);
        } else {
            paramsLeft.setMargins(pixels, 0, pixels, 0);
        }
        return paramsLeft;
    }

    public static LinearLayout createBusArrivalsNoResult(@NonNull final Context context, @NonNull final LinearLayout.LayoutParams containParams, @NonNull final String stopNameTrimmed) {
        return createBusArrivalsLayout(context, containParams, stopNameTrimmed, null, Collections.emptyList());
    }

    // TODO Create XML files instead of doing all those methods in Java
    @NonNull
    public static LinearLayout createBusArrivalsLayout(@NonNull final Context context, @NonNull final LinearLayout.LayoutParams containParams, @NonNull final String stopNameTrimmed, @Nullable final BusDirection.BusDirectionEnum busDirection, @NonNull final List<BusArrival> buses) {
        int pixels = Util.convertDpToPixel(context, 16);
        int pixelsHalf = pixels / 2;
        int marginLeftPixel = Util.convertDpToPixel(context, 10);
        int grey5 = ContextCompat.getColor(context, R.color.grey_5);

        final LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setLayoutParams(containParams);

        // Left
        final LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final RelativeLayout left = new RelativeLayout(context);
        left.setLayoutParams(leftParams);

        final RelativeLayout lineIndication = LayoutUtil.createColoredRoundForFavorites(context, TrainLine.NA);
        int lineId = Util.generateViewId();
        lineIndication.setId(lineId);

        final RelativeLayout.LayoutParams destinationParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        destinationParams.addRule(RelativeLayout.RIGHT_OF, lineId);
        destinationParams.setMargins(pixelsHalf, 0, 0, 0);

        final String leftString = busDirection == null ? stopNameTrimmed : stopNameTrimmed + " " + busDirection.getShortLowerCase();
        final SpannableString destinationSpannable = new SpannableString(leftString);
        destinationSpannable.setSpan(new RelativeSizeSpan(0.65f), stopNameTrimmed.length(), leftString.length(), 0); // set size
        destinationSpannable.setSpan(new ForegroundColorSpan(grey5), 0, leftString.length(), 0); // set color

        final TextView boundCustomTextView = new TextView(context);
        boundCustomTextView.setText(destinationSpannable);
        boundCustomTextView.setSingleLine(true);
        boundCustomTextView.setLayoutParams(destinationParams);

        left.addView(lineIndication);
        left.addView(boundCustomTextView);

        // Right
        final LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rightParams.setMargins(marginLeftPixel, 0, 0, 0);
        final LinearLayout right = new LinearLayout(context);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setLayoutParams(rightParams);

        final StringBuilder currentEtas = new StringBuilder();
        Stream.of(buses).forEach(arri -> currentEtas.append(" ").append(arri.getTimeLeftDueDelay()));

        final TextView arrivalText = new TextView(context);
        arrivalText.setText(currentEtas);
        arrivalText.setGravity(Gravity.END);
        arrivalText.setSingleLine(true);
        arrivalText.setTextColor(grey5);
        arrivalText.setEllipsize(TextUtils.TruncateAt.END);

        right.addView(arrivalText);

        container.addView(left);
        container.addView(right);
        return container;
    }

    @NonNull
    public static LinearLayout createTrainArrivalsLayout(@NonNull final Context context, @NonNull final LinearLayout.LayoutParams containParams, @NonNull final Map.Entry<String, String> entry, @NonNull final TrainLine trainLine) {
        int pixels = Util.convertDpToPixel(context, 16);
        int pixelsHalf = pixels / 2;
        int marginLeftPixel = Util.convertDpToPixel(context, 10);
        int grey5 = ContextCompat.getColor(context, R.color.grey_5);

        final LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setLayoutParams(containParams);

        // Left
        final RelativeLayout.LayoutParams leftParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final RelativeLayout left = new RelativeLayout(context);
        left.setLayoutParams(leftParam);

        final RelativeLayout lineIndication = LayoutUtil.createColoredRoundForFavorites(context, trainLine);
        int lineId = Util.generateViewId();
        lineIndication.setId(lineId);

        final RelativeLayout.LayoutParams destinationParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        destinationParams.addRule(RelativeLayout.RIGHT_OF, lineId);
        destinationParams.setMargins(pixelsHalf, 0, 0, 0);

        final String destination = entry.getKey();
        final TextView destinationTextView = new TextView(context);
        destinationTextView.setTextColor(grey5);
        destinationTextView.setText(destination);
        destinationTextView.setLines(1);
        destinationTextView.setLayoutParams(destinationParams);

        left.addView(lineIndication);
        left.addView(destinationTextView);

        // Right
        final LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rightParams.setMargins(marginLeftPixel, 0, 0, 0);
        final LinearLayout right = new LinearLayout(context);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setLayoutParams(rightParams);

        final String currentEtas = entry.getValue();
        final TextView arrivalText = new TextView(context);
        arrivalText.setText(currentEtas);
        arrivalText.setGravity(Gravity.END);
        arrivalText.setSingleLine(true);
        arrivalText.setTextColor(grey5);
        arrivalText.setEllipsize(TextUtils.TruncateAt.END);

        right.addView(arrivalText);

        container.addView(left);
        container.addView(right);

        return container;
    }

    @NonNull
    public static LinearLayout createBikeLayout(@NonNull final Context context, @NonNull final BikeStation bikeStation) {
        final LinearLayout.LayoutParams containerParams = getInsideParams(context, true, true);
        final LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(containerParams);

        final LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(createBikeLine(context, bikeStation, true));
        linearLayout.addView(createBikeLine(context, bikeStation, false));

        container.addView(linearLayout);
        return container;
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

        final RelativeLayout lineIndication = createColoredRoundForFavorites(context, TrainLine.NA);
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
