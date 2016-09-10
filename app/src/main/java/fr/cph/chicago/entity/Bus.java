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

import java.security.Timestamp;
import java.util.List;

import lombok.Data;

@Data
public class Bus {
    private int id;
    private Timestamp timestamp;
    private Position position;
    private int heading;
    private int patternId;
    private int patternDistance;
    private String routeId;
    private String destination;
    private Boolean delay;

    private Bus(final BusBuilder busBuilder) {
        this.id = busBuilder.id;
        this.timestamp = busBuilder.timestamp;
        this.position = busBuilder.position;
        this.heading = busBuilder.heading;
        this.patternId = busBuilder.patternId;
        this.patternDistance = busBuilder.patternDistance;
        this.routeId = busBuilder.routeId;
        this.destination = busBuilder.destination;
        this.delay = busBuilder.delay;
    }

    @NonNull
    public static Position getBestPosition(@NonNull final List<Bus> buses) {
        final Position position = new Position();
        double maxLatitude = 0.0;
        double minLatitude = 0.0;
        double maxLongitude = 0.0;
        double minLongitude = 0.0;
        int i = 0;
        for (final Bus bus : buses) {
            final Position temp = bus.getPosition();
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

    public static BusBuilder builder() {
        return new BusBuilder();
    }

    public static class BusBuilder {
        private int id;
        private Timestamp timestamp;
        private Position position;
        private int heading;
        private int patternId;
        private int patternDistance;
        private String routeId;
        private String destination;
        private Boolean delay;

        BusBuilder() {
        }

        public BusBuilder id(int id) {
            this.id = id;
            return this;
        }

        public BusBuilder timestamp(Timestamp timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public BusBuilder position(Position position) {
            this.position = position;
            return this;
        }

        public BusBuilder heading(int heading) {
            this.heading = heading;
            return this;
        }

        public BusBuilder patternId(int patternId) {
            this.patternId = patternId;
            return this;
        }

        public BusBuilder patternDistance(int patternDistance) {
            this.patternDistance = patternDistance;
            return this;
        }

        public BusBuilder routeId(String routeId) {
            this.routeId = routeId;
            return this;
        }

        public BusBuilder destination(String destination) {
            this.destination = destination;
            return this;
        }

        public BusBuilder delay(Boolean delay) {
            this.delay = delay;
            return this;
        }

        public Bus build() {
            return new Bus(this);
        }
    }
}
