/**
 * Copyright 2020 Carl-Philipp Harmant
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

package fr.cph.chicago.core.model

import android.os.Parcel
import android.os.Parcelable
import org.apache.commons.lang3.StringUtils
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Bus Arrival entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
data class BusArrival(
    val timeStamp: Date,
    val errorMessage: String = StringUtils.EMPTY,
    val stopName: String,
    val stopId: Int,
    val busId: Int,
    val routeId: String,
    val routeDirection: String,
    val busDestination: String,
    val predictionTime: Date,
    val isDelay: Boolean) : Parcelable {

    companion object {
        private const val NO_SERVICE = "No service"

        @JvmField
        val CREATOR: Parcelable.Creator<BusArrival> = object : Parcelable.Creator<BusArrival> {
            override fun createFromParcel(source: Parcel): BusArrival {
                return BusArrival(source)
            }

            override fun newArray(size: Int): Array<BusArrival?> {
                return arrayOfNulls(size)
            }
        }
    }

    private constructor(source: Parcel) : this(
        timeStamp = Date(source.readLong()),
        errorMessage = source.readString() ?: StringUtils.EMPTY,
        stopName = source.readString() ?: StringUtils.EMPTY,
        stopId = source.readInt(),
        busId = source.readInt(),
        routeId = source.readString() ?: StringUtils.EMPTY,
        routeDirection = source.readString() ?: StringUtils.EMPTY,
        busDestination = source.readString() ?: StringUtils.EMPTY,
        predictionTime = Date(source.readLong()),
        isDelay = source.readString()!!.toBoolean())

    val timeLeftMinutes: String
        get() {
            return if (StringUtils.EMPTY == errorMessage) {
                val time = predictionTime.time - timeStamp.time
                String.format(Locale.ENGLISH, "%d min", TimeUnit.MILLISECONDS.toMinutes(time))
            } else {
                NO_SERVICE
            }
        }

    val timeLeftMilli: Long
        get() {
            return predictionTime.time - timeStamp.time
        }


    val timeLeftDueDelay: String
        get() {
            return if (isDelay) {
                "Delay"
            } else {
                if ("0 min" == timeLeftMinutes.trim { it <= ' ' }) {
                    "Due"
                } else {
                    timeLeftMinutes
                }
            }
        }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(timeStamp.time)
        dest.writeString(errorMessage)
        dest.writeString(stopName)
        dest.writeInt(stopId)
        dest.writeInt(busId)
        dest.writeString(routeId)
        dest.writeString(routeDirection)
        dest.writeString(busDestination)
        dest.writeLong(predictionTime.time)
        dest.writeString(isDelay.toString())
    }
}
