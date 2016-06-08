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

package fr.cph.chicago.entity;

import android.support.annotation.NonNull;

import java.util.List;

import lombok.Data;

@Data
public class Train {

    private int routeNumber;
    private int destStation;
    private String destName;
    private int routeDirection;
    private boolean app;
    private boolean dly;
    private Position position;
    private int heading;

    @NonNull
    public static Position getBestPosition(@NonNull final List<Train> trains) {
        final Position position = new Position();
        double maxLatitude = 0.0;
        double minLatitude = 0.0;
        double maxLongitude = 0.0;
        double minLongitude = 0.0;
        int i = 0;
        for (final Train train : trains) {
            Position temp = train.getPosition();
            if (i == 0) {
                maxLatitude = temp.getLatitude();
                minLatitude = temp.getLatitude();
                maxLongitude = temp.getLongitude();
                minLongitude = temp.getLongitude();
            } else {
                if (temp.getLatitude() > maxLatitude) {
                    maxLatitude = temp.getLatitude();
                }
                if (temp.getLatitude() < minLatitude) {
                    minLatitude = temp.getLatitude();
                }
                if (temp.getLongitude() > maxLongitude) {
                    maxLongitude = temp.getLongitude();
                }
                if (temp.getLongitude() < minLongitude) {
                    minLongitude = temp.getLongitude();
                }
            }
            i++;
        }
        position.setLatitude((maxLatitude + minLatitude) / 2);
        position.setLongitude((maxLongitude + minLongitude) / 2);
        return position;
    }
}
