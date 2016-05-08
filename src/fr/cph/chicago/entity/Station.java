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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import fr.cph.chicago.entity.enumeration.TrainLine;
import lombok.Data;

/**
 * Station entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
@Data
public class Station implements Comparable<Station>, Parcelable {
    /**
     * The id
     **/
    private int id;
    /**
     * The name
     **/
    private String name;
    /**
     * The stops list
     **/
    private List<Stop> stops;

    public Station() {
    }

    private Station(@NonNull final Parcel in) {
        readFromParcel(in);
    }

    @Override
    public final String toString() {
        StringBuilder stb = new StringBuilder();
        stb.append("[Id=").append(id);
        stb.append(";name=").append(name);
        if (stops != null) {
            stb.append(";stops=").append(stops);
        }
        if (getLines().size() > 0) {
            stb.append(";lines=").append(getLines());
        }
        stb.append("]");
        return stb.toString();
    }

    @NonNull
    public final Set<TrainLine> getLines() {
        if (stops != null) {
            final Set<TrainLine> lines = new TreeSet<>();
            for (final Stop stop : stops) {
                for (final TrainLine trainLine : stop.getLines()) {
                    lines.add(trainLine);
                }
            }
            return lines;
        } else {
            return Collections.emptySet();
        }
    }

    @NonNull
    public final Map<TrainLine, List<Stop>> getStopByLines() {
        Map<TrainLine, List<Stop>> result = new TreeMap<>();
        List<Stop> stops = getStops();
        for (final Stop stop : stops) {
            List<TrainLine> lines = stop.getLines();
            for (TrainLine tl : lines) {
                List<Stop> stopss;
                if (result.containsKey(tl)) {
                    stopss = result.get(tl);
                    stopss.add(stop);
                } else {
                    stopss = new ArrayList<>();
                    stopss.add(stop);
                    result.put(tl, stopss);
                }
            }
        }
        return result;
    }

    @Override
    public final int compareTo(@NonNull final Station another) {
        return this.getName().compareTo(another.getName());
    }

    @NonNull
    public List<Position> getStopsPosition() {
        final List<Position> positions = new ArrayList<>();
        for (Stop stop : stops) {
            positions.add(stop.getPosition());
        }
        return positions;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeList(stops);
    }

    private void readFromParcel(@NonNull final Parcel in) {
        id = in.readInt();
        name = in.readString();
        stops = new ArrayList<>();
        in.readList(stops, Stop.class.getClassLoader());
    }

    public static final Parcelable.Creator<Station> CREATOR = new Parcelable.Creator<Station>() {
        public Station createFromParcel(final Parcel in) {
            return new Station(in);
        }

        public Station[] newArray(final int size) {
            return new Station[size];
        }
    };

}
