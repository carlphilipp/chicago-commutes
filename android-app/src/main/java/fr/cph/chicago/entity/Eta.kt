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
    var station: Station? = null,
    var stop: Stop? = null,
    var runNumber: Int = 0,
    var routeName: TrainLine? = null,
    var destSt: Int = 0,
    var destName: String? = null,
    var trainRouteDirectionCode: Int = 0,
    var predictionDate: Date? = null,
    var arrivalDepartureDate: Date? = null,
    var isApp: Boolean = false,
    var isDly: Boolean = false,
    var flags: String? = null,
    var position: Position? = null,
    var heading: Int = 0) : Comparable<Eta>, Parcelable, Serializable {

    constructor(source: Parcel) : this(
        source.readParcelable<Station>(Station::class.java.classLoader),
        source.readParcelable<Stop>(Stop::class.java.classLoader),
        source.readInt(),
        TrainLine.fromXmlString(source.readString()),
        source.readInt(),
        source.readString(),
        source.readInt(),
        Date(source.readLong()),
        Date(source.readLong()),
        java.lang.Boolean.valueOf(source.readString())!!,
        java.lang.Boolean.valueOf(source.readString())!!,
        source.readString(),
        source.readParcelable<Position>(Position::class.java.classLoader),
        source.readInt()
    )

    private val timeLeft: String
        get() {
            val time = arrivalDepartureDate!!.time - predictionDate!!.time
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
        val time1 = arrivalDepartureDate!!.time - predictionDate!!.time
        val time2 = other.arrivalDepartureDate!!.time - other.predictionDate!!.time
        return time1.compareTo(time2)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(station, flags)
        dest.writeParcelable(stop, flags)
        dest.writeInt(runNumber)
        dest.writeString(routeName!!.toTextString())
        dest.writeInt(destSt)
        dest.writeString(destName)
        dest.writeInt(trainRouteDirectionCode)
        dest.writeLong(predictionDate!!.time)
        dest.writeLong(arrivalDepartureDate!!.time)
        dest.writeString(isApp.toString())
        dest.writeString(isDly.toString())
        dest.writeString(this.flags)
        dest.writeParcelable(position, flags)
        dest.writeInt(heading)
    }

    override fun toString(): String {
        return "Eta(station=$station, stop=$stop, runNumber=$runNumber, routeName=$routeName, destSt=$destSt, destName=$destName, trainRouteDirectionCode=$trainRouteDirectionCode, predictionDate=$predictionDate, arrivalDepartureDate=$arrivalDepartureDate, isApp=$isApp, isDelay=$isDly, flags=$flags, position=$position, heading=$heading)"
    }

    companion object {

        private const val serialVersionUID = 0L

        fun buildFakeEtaWith(station: Station, arrivalDepartureDate: Date, predictionDate: Date, app: Boolean, delay: Boolean): Eta {
            return Eta(station, Stop(), 0, TrainLine.NA, 0, StringUtils.EMPTY, 0, predictionDate, arrivalDepartureDate, app, delay, "", Position(), 0)
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
