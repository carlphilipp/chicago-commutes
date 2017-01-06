/**
 * Copyright 2017 Carl-Philipp Harmant
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.entity.enumeration;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Locale;

/**
 * Enumeration, bus direction
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */

public class BusDirection {

    private static final String TAG = BusDirection.class.getSimpleName();

    private final String textReceived;
    private BusDirectionEnum busDirectionEnum;

    public BusDirection(@NonNull final String textReceived) {
        this.textReceived = textReceived;
    }

    public boolean isOk() {
        try {
            busDirectionEnum = BusDirectionEnum.fromString(textReceived);
            return true;
        }catch (final Exception e){
            return false;
        }
    }

    @NonNull
    public String getBusTextReceived() {
        return textReceived;
    }

    @NonNull
    public BusDirectionEnum getBusDirectionEnum() {
        return busDirectionEnum;
    }

    public enum BusDirectionEnum {

        NORTHBOUND("Northbound", "NORTH", "North"), WESTBOUND("Westbound", "WEST", "West"), SOUTHBOUND("Southbound", "SOUTH", "South"), EASTBOUND("Eastbound", "EAST", "East");

        private final String text;
        private final String shortUpperCase;
        private final String shortLowerCase;

        BusDirectionEnum(final String text, final String shortUpperCase, final String shortLowerCase) {
            this.text = text;
            this.shortUpperCase = shortUpperCase;
            this.shortLowerCase = shortLowerCase;
        }

        @NonNull
        public static BusDirectionEnum fromString(@NonNull final String text) {
            for (final BusDirectionEnum busDirectionEnum : BusDirectionEnum.values()) {
                if (text.equalsIgnoreCase(busDirectionEnum.text)) {
                    return busDirectionEnum;
                } else if (text.equalsIgnoreCase(busDirectionEnum.shortUpperCase)) {
                    return busDirectionEnum;
                } else if (busDirectionEnum.text.toLowerCase(Locale.US).contains(text.toLowerCase(Locale.US))) {
                    return busDirectionEnum;
                }
            }
            Log.w(TAG, "Bus direction enum not found: " + text);
            throw new IllegalStateException();
        }

        @Override
        public final String toString() {
            return text;
        }

        @NonNull
        public final String getShortUpperCase() {
            return shortUpperCase;
        }

        @NonNull
        public final String getShortLowerCase() {
            return shortLowerCase;
        }
    }
}

