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

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import fr.cph.chicago.app.App;
import fr.cph.chicago.R;

/**
 * Enumeration, train line
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public enum TrainLine {

    BLUE("Blue", "Blue", Color.rgb(0, 158, 218)),
    BROWN("Brn", "Brown", Color.rgb(102, 51, 0)),
    GREEN("G", "Green", Color.rgb(0, 153, 0)),
    ORANGE("Org", "Orange", Color.rgb(255, 128, 0)),
    PINK("Pink", "Pink", Color.rgb(204, 0, 102)),
    PURPLE("P", "Purple", Color.rgb(102, 0, 102)),
    RED("Red", "Red", Color.rgb(240, 0, 0)),
    YELLOW("Y", "Yellow", ContextCompat.getColor(App.getContext(), R.color.yellowLine)),
    NA("N/A", "N/A", Color.BLACK);

    /**
     * The text
     **/
    private final String text;
    /**
     * The name
     **/
    private final String name;
    /**
     * THe color
     **/
    private final int color;

    /**
     * Private constructor
     *
     * @param text     the text
     * @param name     the name
     * @param color    the color
     */
    TrainLine(final String text, final String name, final int color) {
        this.text = text;
        this.name = name;
        this.color = color;
    }

    /**
     * The train line from xml string
     *
     * @param text the text
     * @return the text
     */
    @NonNull
    public static TrainLine fromXmlString(@NonNull final String text) {
        for (final TrainLine b : TrainLine.values()) {
            if (text.equalsIgnoreCase(b.text)) {
                return b;
            }
        }
        return NA;
    }

    /**
     * The train line from string
     *
     * @param text the text
     * @return a train line
     */
    @NonNull
    public static TrainLine fromString(@Nullable final String text) {
        for (final TrainLine b : TrainLine.values()) {
            if (b.name.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return NA;
    }

    /**
     * Get to string with line
     */
    @NonNull
    public final String toStringWithLine() {
        return name + " Line";
    }

    /**
     * Get color
     *
     * @return the color
     */
    public final int getColor() {
        return color;
    }

    @Override
    public final String toString() {
        return name;
    }

    @NonNull
    public final String toTextString() {
        return text;
    }

    public static int size() {
        return TrainLine.values().length;
    }
}
