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
import java.util.Date;
import java.util.List;

import fr.cph.chicago.entity.enumeration.TrainLine;
import lombok.Data;

/**
 * Train Arrival entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
@Data
public class TrainArrival implements Parcelable {
    /**
     * The timestamp
     **/
    private Date timeStamp;
    /**
     * The error code
     **/
    private int errorCode;
    /**
     * The error message
     **/
    private String errorMessage;
    /**
     * A list of Eta
     **/
    private List<Eta> etas;

    /**
     *
     */
    public TrainArrival() {
    }

    /**
     * @param in
     */
    private TrainArrival(@NonNull final Parcel in) {
        readFromParcel(in);
    }

    /**
     * @param line
     * @return
     */
    @NonNull
    public final List<Eta> getEtas(@NonNull final TrainLine line) {
        List<Eta> etas = new ArrayList<>();
        for (final Eta eta : getEtas()) {
            if (eta.getRouteName() == line) {
                etas.add(eta);
            }
        }
        return etas;
    }

    @Override
    public final int describeContents() {
        return 0;
    }

    @Override
    public final void writeToParcel(final Parcel dest, final int flags) {
//		dest.writeLong(timeStamp.getTime());
//		dest.writeInt(errorCode);
//		dest.writeString(errorMessage);
        dest.writeList(etas);
    }

    private void readFromParcel(final Parcel in) {
//		timeStamp = new Date(in.readLong());
//		errorCode = in.readInt();
//		errorMessage = in.readString();
        etas = new ArrayList<>();
        in.readList(etas, Eta.class.getClassLoader());
    }

    public static final Parcelable.Creator<TrainArrival> CREATOR = new Parcelable.Creator<TrainArrival>() {
        public TrainArrival createFromParcel(final Parcel in) {
            return new TrainArrival(in);
        }

        public TrainArrival[] newArray(final int size) {
            return new TrainArrival[size];
        }
    };
}
