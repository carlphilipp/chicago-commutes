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

package fr.cph.chicago.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author carl
 */
@AllArgsConstructor
@Builder
@Data
public final class BusPattern implements Parcelable {

    private int id;
    private double length;
    private String direction;
    private List<PatternPoint> points;

    public final void addPoint(@NonNull final PatternPoint patternPoint) {
        if (points == null) {
            points = new ArrayList<>();
        }
        this.points.add(patternPoint);
    }

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
