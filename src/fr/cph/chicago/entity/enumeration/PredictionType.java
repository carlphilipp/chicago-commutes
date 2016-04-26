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

package fr.cph.chicago.entity.enumeration;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Enumeration, prediction type
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public enum PredictionType {
    ARRIVAL("A"), DEPARTURE("D");

    /**
     * The message
     **/
    private final String message;

    /**
     * Private constructor
     *
     * @param message
     */
    PredictionType(final String message) {
        this.message = message;
    }

    /**
     * Get Prediction type from string
     *
     * @param text the text
     * @return a prediction type
     */
    @Nullable
    public static PredictionType fromString(@NonNull final String text) {
        for (final PredictionType b : PredictionType.values()) {
            if (text.equalsIgnoreCase(b.message)) {
                return b;
            }
        }
        return null;
    }

    @Override
    public final String toString() {
        return this.message;
    }
}
