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

import lombok.Data;

@Data
public class PatternPoint implements Parcelable {
    /**
     * Sequence number
     **/
    private int sequence;
    /**
     * The position
     **/
    private Position position;
    /**
     * The type
     **/
    private String type;
    /**
     * The stop id
     **/
    private int stopId;
    /**
     * The stop name
     **/
    private String stopName;
    /**
     * The distance
     **/
    private double distance;

    /**
     *
     */
    public PatternPoint() {
    }

    /**
     * @param in
     */
    private PatternPoint(final Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sequence);
        dest.writeParcelable(position, flags);
        dest.writeString(type);
        dest.writeInt(stopId);
        dest.writeString(stopName);
        dest.writeDouble(distance);
    }

    private void readFromParcel(Parcel in) {
        sequence = in.readInt();
        position = in.readParcelable(Position.class.getClassLoader());
        type = in.readString();
        stopId = in.readInt();
        stopName = in.readString();
        distance = in.readDouble();
    }

    public static final Parcelable.Creator<PatternPoint> CREATOR = new Parcelable.Creator<PatternPoint>() {
        public PatternPoint createFromParcel(Parcel in) {
            return new PatternPoint(in);
        }

        public PatternPoint[] newArray(int size) {
            return new PatternPoint[size];
        }
    };
}
