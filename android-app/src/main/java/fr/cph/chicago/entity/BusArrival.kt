/**
 * Copyright 2017 Carl-Philipp Harmant
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.entity

import android.os.Parcel
import android.os.Parcelable
import fr.cph.chicago.entity.enumeration.PredictionType
import java.util.*

/**
 * Bus Arrival entity

 * @author Carl-Philipp Harmant
 * *
 * @version 1
 */
class BusArrival(
    var timeStamp: Date? = null,
    val errorMessage: String? = null,
    val predictionType: PredictionType? = null,
    var stopName: String? = null,
    var stopId: Int = 0,
    var busId: Int = 0,
    val distanceToStop: Int = 0, // feets
    var routeId: String? = null,
    var routeDirection: String? = null,
    var busDestination: String? = null,
    var predictionTime: Date? = null,
    var isDly: Boolean = false) : Parcelable {

    private constructor(`in`: Parcel) : this() {
        readFromParcel(`in`)
    }

    val timeLeft: String
        get() {
            if (predictionTime != null && timeStamp != null) {
                //val time = predictionTime?.time - timeStamp?.time
                //return String.format(Locale.ENGLISH, "%d min", TimeUnit.MILLISECONDS.toMinutes(time))
                return "time"
            } else {
                return NO_SERVICE
            }
        }

    val timeLeftDueDelay: String
        get() {
            val result: String
            if (isDly) {
                result = "Delay"
            } else {
                if ("0 min" == timeLeft.trim { it <= ' ' }) {
                    result = "Due"
                } else {
                    result = timeLeft
                }
            }
            return result
        }

    override fun hashCode(): Int {
        var result = if (timeStamp != null) timeStamp!!.hashCode() else 0
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + (predictionType?.hashCode() ?: 0)
        result = 31 * result + if (stopName != null) stopName!!.hashCode() else 0
        result = 31 * result + stopId
        result = 31 * result + busId
        result = 31 * result + distanceToStop
        result = 31 * result + if (routeId != null) routeId!!.hashCode() else 0
        result = 31 * result + if (routeDirection != null) routeDirection!!.hashCode() else 0
        result = 31 * result + if (busDestination != null) busDestination!!.hashCode() else 0
        result = 31 * result + if (predictionTime != null) predictionTime!!.hashCode() else 0
        result = 31 * result + if (isDly) 1 else 0
        return result
    }

    override fun equals(o: Any?): Boolean {
        if (this === o)
            return true
        if (o == null || javaClass != o.javaClass)
            return false

        val that = o as BusArrival?

        if (stopId != that!!.stopId)
            return false
        if (busId != that.busId)
            return false
        if (distanceToStop != that.distanceToStop)
            return false
        if (isDly != that.isDly)
            return false
        if (if (timeStamp != null) timeStamp != that.timeStamp else that.timeStamp != null)
            return false
        if (if (errorMessage != null) errorMessage != that.errorMessage else that.errorMessage != null)
            return false
        if (predictionType != that.predictionType)
            return false
        if (if (stopName != null) stopName != that.stopName else that.stopName != null)
            return false
        if (if (routeId != null) routeId != that.routeId else that.routeId != null)
            return false
        if (if (routeDirection != null) routeDirection != that.routeDirection else that.routeDirection != null)
            return false
        if (if (busDestination != null) busDestination != that.busDestination else that.busDestination != null)
            return false
        return if (predictionTime != null) predictionTime == that.predictionTime else that.predictionTime == null

    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(timeStamp!!.time)
        //dest.writeString(errorMessage);
        //dest.writeString(predictionType.toString());
        dest.writeString(stopName)
        dest.writeInt(stopId)
        dest.writeInt(busId)
        //dest.writeInt(distanceToStop);
        dest.writeString(routeId)
        dest.writeString(routeDirection)
        dest.writeString(busDestination)
        dest.writeLong(predictionTime!!.time)
        dest.writeString(isDly.toString())
    }

    private fun readFromParcel(`in`: Parcel) {
        timeStamp = Date(`in`.readLong())
        //errorMessage = in.readString();
        //predictionType = PredictionType.fromString(in.readString());
        stopName = `in`.readString()
        stopId = `in`.readInt()
        busId = `in`.readInt()
        //distanceToStop = in.readInt();
        routeId = `in`.readString()
        routeDirection = `in`.readString()
        busDestination = `in`.readString()
        predictionTime = Date(`in`.readLong())
        isDly = java.lang.Boolean.parseBoolean(`in`.readString())
    }

    companion object {

        private val NO_SERVICE = "No service"

        val CREATOR: Parcelable.Creator<BusArrival> = object : Parcelable.Creator<BusArrival> {
            override fun createFromParcel(`in`: Parcel): BusArrival {
                return BusArrival(`in`)
            }

            override fun newArray(size: Int): Array<BusArrival> {
                // FIXME parcelable kotlin
                return arrayOf()
            }
        }
    }
}
