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
import fr.cph.chicago.entity.enumeration.TrainLine
import org.apache.commons.lang3.StringUtils
import java.io.Serializable
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Eta entity

 * @author Carl-Philipp Harmant
 * *
 * @version 1
 */
class Eta(
    val station: Station,
    val stop: Stop,
    val routeName: TrainLine,
    val destName: String,
    val predictionDate: Date,
    val arrivalDepartureDate: Date,
    val isApp: Boolean,
    var isDly: Boolean,
    val position: Position) : Comparable<Eta>, Parcelable, Serializable {

    constructor(source: Parcel) : this(
        source.readParcelable<Station>(Station::class.java.classLoader),
        source.readParcelable<Stop>(Stop::class.java.classLoader),
        TrainLine.fromXmlString(source.readString()),
        source.readString(),
        Date(source.readLong()),
        Date(source.readLong()),
        java.lang.Boolean.valueOf(source.readString())!!,
        java.lang.Boolean.valueOf(source.readString())!!,
        source.readParcelable<Position>(Position::class.java.classLoader)
    )

    private val timeLeft: String
        get() {
            val time = arrivalDepartureDate.time - predictionDate.time
            return String.format(Locale.ENGLISH, "%d min", TimeUnit.MILLISECONDS.toMinutes(time))
        }

    val timeLeftDueDelay: String
        get() {
            val result: String
            if (isDly) {
                result = "Delay"
            } else {
                if (isApp) {
                    result = "Due"
                } else {
                    result = timeLeft
                }
            }
            return result
        }

    override fun compareTo(other: Eta): Int {
        val time1 = arrivalDepartureDate.time - predictionDate.time
        val time2 = other.arrivalDepartureDate.time - other.predictionDate.time
        return time1.compareTo(time2)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(station, flags)
        dest.writeParcelable(stop, flags)
        dest.writeString(routeName.toTextString())
        dest.writeString(destName)
        dest.writeLong(predictionDate.time)
        dest.writeLong(arrivalDepartureDate.time)
        dest.writeString(isApp.toString())
        dest.writeString(isDly.toString())
        dest.writeParcelable(position, flags)
    }

    override fun toString(): String {
        return "Eta(station=$station, stop=$stop, routeName=$routeName, destName=$destName, predictionDate=$predictionDate, arrivalDepartureDate=$arrivalDepartureDate, isApp=$isApp, isDelay=$isDly, position=$position)"
    }

    companion object {

        private const val serialVersionUID = 0L

        fun buildFakeEtaWith(station: Station, arrivalDepartureDate: Date, predictionDate: Date, app: Boolean, delay: Boolean): Eta {
            return Eta(station, Stop(), TrainLine.NA, StringUtils.EMPTY, predictionDate, arrivalDepartureDate, app, delay, Position())
        }

        @JvmField val CREATOR: Parcelable.Creator<Eta> = object : Parcelable.Creator<Eta> {
            override fun createFromParcel(source: Parcel): Eta {
                return Eta(source)
            }

            override fun newArray(size: Int): Array<Eta?> {
                return arrayOfNulls(size)
            }
        }
    }
}
