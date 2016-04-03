/**
 * Copyright 2016 Carl-Philipp Harmant
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

import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import fr.cph.chicago.App;
import fr.cph.chicago.entity.enumeration.TrainLine;

/**
 * Layout util class
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class LayoutUtil {

    private LayoutUtil() {
    }

    @NonNull
    public static LinearLayout createColoredRoundForFavorites(@NonNull final TrainLine trainLine) {
        final LinearLayout lineIndication = new LinearLayout(App.getContext());
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.height = 30;
        params.width = 30;
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        lineIndication.setBackgroundColor(trainLine.getColor());
        lineIndication.setLayoutParams(params);
        return lineIndication;
    }

    @NonNull
    public static LinearLayout createColoredRoundForMultiple(@NonNull final TrainLine trainLine) {
        final LinearLayout lineIndication = new LinearLayout(App.getContext());
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.height = 30;
        params.width = 30;
        params.setMargins(10, 0, 0, 0);
        lineIndication.setBackgroundColor(trainLine.getColor());
        lineIndication.setLayoutParams(params);
        return lineIndication;
    }
}
