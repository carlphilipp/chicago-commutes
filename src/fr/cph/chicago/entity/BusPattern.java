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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * @author carl
 */
@Data
public final class BusPattern implements Parcelable {
    /**
     * The pattern id
     **/
    private int id;
    /**
     * The length in feet
     **/
    private double length;
    /**
     * The direction
     **/
    private String direction;
    /**
     * The list of points
     **/
    private List<PatternPoint> points;

    /**
     *
     */
    public BusPattern() {
        this.points = new ArrayList<>();
    }

    public final void addPoint(@NonNull PatternPoint patternPoint) {
        this.points.add(patternPoint);
    }

    /**
     * @param in
     */
    private BusPattern(@NonNull final Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(id);
        dest.writeDouble(length);
        dest.writeString(direction);
        dest.writeList(points);
    }

    private void readFromParcel(@NonNull final Parcel in) {
        id = in.readInt();
        length = in.readDouble();
        direction = in.readString();
        in.readList(points, PatternPoint.class.getClassLoader());
    }

    public static final Parcelable.Creator<BusPattern> CREATOR = new Parcelable.Creator<BusPattern>() {
        public BusPattern createFromParcel(Parcel in) {
            return new BusPattern(in);
        }

        public BusPattern[] newArray(int size) {
            return new BusPattern[size];
        }
    };
}
