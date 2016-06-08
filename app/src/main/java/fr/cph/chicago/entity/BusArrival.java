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

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import fr.cph.chicago.entity.enumeration.PredictionType;
import lombok.Data;

/**
 * Bus Arrival entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
@Data
public final class BusArrival implements Parcelable {
    /** **/
    private static final String NO_SERVICE = "No service";
    /**
     * Timestamp
     **/
    private Date timeStamp;
    /**
     * Error message
     **/
    private String errorMessage;
    /**
     * TYpe of prediction
     **/
    private PredictionType predictionType;
    /**
     * Stop name
     **/
    private String stopName;
    /**
     * Stop id
     **/
    private int stopId;
    /**
     * Bus id
     **/
    private int busId;
    /**
     * Distance to stop
     **/
    private int distanceToStop; // feets
    /**
     * Route id
     **/
    private String routeId;
    /**
     * Route direction
     **/
    private String routeDirection;
    /**
     * Bus destination
     **/
    private String busDestination;
    /**
     * Prediction time
     **/
    private Date predictionTime;
    /**
     * Is delayed
     **/
    private boolean isDly;

    /**
     *
     */
    public BusArrival() {

    }

    private BusArrival(final Parcel in) {
        readFromParcel(in);
    }

    @NonNull
    public final String getTimeLeft() {
        if (getPredictionTime() != null && getTimeStamp() != null) {
            long time = getPredictionTime().getTime() - getTimeStamp().getTime();
            return String.format(Locale.ENGLISH, "%d min", TimeUnit.MILLISECONDS.toMinutes(time));
        } else {
            return NO_SERVICE;
        }
    }

    @NonNull
    public final String getTimeLeftDueDelay() {
        String result;
        if (isDly()) {
            result = "Delay";
        } else {
            if ("0 min".equals(getTimeLeft().trim())) {
                result = "Due";
            } else {
                result = getTimeLeft();
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        int result = timeStamp != null ? timeStamp.hashCode() : 0;
        result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
        result = 31 * result + (predictionType != null ? predictionType.hashCode() : 0);
        result = 31 * result + (stopName != null ? stopName.hashCode() : 0);
        result = 31 * result + stopId;
        result = 31 * result + busId;
        result = 31 * result + distanceToStop;
        result = 31 * result + (routeId != null ? routeId.hashCode() : 0);
        result = 31 * result + (routeDirection != null ? routeDirection.hashCode() : 0);
        result = 31 * result + (busDestination != null ? busDestination.hashCode() : 0);
        result = 31 * result + (predictionTime != null ? predictionTime.hashCode() : 0);
        result = 31 * result + (isDly ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BusArrival that = (BusArrival) o;

        if (stopId != that.stopId)
            return false;
        if (busId != that.busId)
            return false;
        if (distanceToStop != that.distanceToStop)
            return false;
        if (isDly != that.isDly)
            return false;
        if (timeStamp != null ? !timeStamp.equals(that.timeStamp) : that.timeStamp != null)
            return false;
        if (errorMessage != null ? !errorMessage.equals(that.errorMessage) : that.errorMessage != null)
            return false;
        if (predictionType != that.predictionType)
            return false;
        if (stopName != null ? !stopName.equals(that.stopName) : that.stopName != null)
            return false;
        if (routeId != null ? !routeId.equals(that.routeId) : that.routeId != null)
            return false;
        if (routeDirection != null ? !routeDirection.equals(that.routeDirection) : that.routeDirection != null)
            return false;
        if (busDestination != null ? !busDestination.equals(that.busDestination) : that.busDestination != null)
            return false;
        return predictionTime != null ? predictionTime.equals(that.predictionTime) : that.predictionTime == null;

    }

    @Override
    public final int describeContents() {
        return 0;
    }

    @Override
    public final void writeToParcel(final Parcel dest, final int flags) {
        dest.writeLong(timeStamp.getTime());
        //dest.writeString(errorMessage);
        //dest.writeString(predictionType.toString());
        dest.writeString(stopName);
        dest.writeInt(stopId);
        dest.writeInt(busId);
        //dest.writeInt(distanceToStop);
        dest.writeString(routeId);
        dest.writeString(routeDirection);
        dest.writeString(busDestination);
        dest.writeLong(predictionTime.getTime());
        dest.writeString(String.valueOf(isDly));
    }

    private void readFromParcel(@NonNull final Parcel in) {
        timeStamp = new Date(in.readLong());
        //errorMessage = in.readString();
        //predictionType = PredictionType.fromString(in.readString());
        stopName = in.readString();
        stopId = in.readInt();
        busId = in.readInt();
        //distanceToStop = in.readInt();
        routeId = in.readString();
        routeDirection = in.readString();
        busDestination = in.readString();
        predictionTime = new Date(in.readLong());
        isDly = Boolean.parseBoolean(in.readString());
    }

    public static final Parcelable.Creator<BusArrival> CREATOR = new Parcelable.Creator<BusArrival>() {
        public BusArrival createFromParcel(Parcel in) {
            return new BusArrival(in);
        }

        public BusArrival[] newArray(int size) {
            return new BusArrival[size];
        }
    };

    public static List<BusArrival> getRealBusArrival(final List<BusArrival> arrivals) {
        return Stream.of(arrivals)
            .filter(arrival -> !arrival.getTimeLeft().equals(NO_SERVICE))
            .collect(Collectors.toList());
    }
}
