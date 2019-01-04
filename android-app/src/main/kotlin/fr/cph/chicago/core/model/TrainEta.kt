/**
 * Copyright 2019 Carl-Philipp Harmant
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
import fr.cph.chicago.core.model.enumeration.TrainLine
import org.apache.commons.lang3.StringUtils
import java.io.Serializable
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * TrainEta entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
data class TrainEta(
    val trainStation: TrainStation,
    val stop: Stop,
    val routeName: TrainLine,
    val destName: String,
    private val predictionDate: Date,
    private val arrivalDepartureDate: Date,
    private val isApp: Boolean,
    private var isDly: Boolean) : Comparable<TrainEta>, Parcelable, Serializable {

    constructor(source: Parcel) : this(
        trainStation = source.readParcelable<TrainStation>(TrainStation::class.java.classLoader)
            ?: TrainStation.buildEmptyStation(),
        stop = source.readParcelable<Stop>(Stop::class.java.classLoader) ?: Stop.buildEmptyStop(),
        routeName = TrainLine.fromXmlString(source.readString() ?: ""),
        destName = source.readString() ?: "",
        predictionDate = Date(source.readLong()),
        arrivalDepartureDate = Date(source.readLong()),
        isApp = source.readString()?.toBoolean() ?: false,
        isDly = source.readString()?.toBoolean() ?: false
    )

    private val timeLeft: String
        get() {
            val time = arrivalDepartureDate.time - predictionDate.time
            return String.format(Locale.ENGLISH, "%d min", TimeUnit.MILLISECONDS.toMinutes(time))
        }

    val timeLeftDueDelay: String
        get() {
            return if (isDly) {
                "Delay"
            } else {
                if (isApp) "Due" else timeLeft
            }
        }

    override fun compareTo(other: TrainEta): Int {
        val time1 = arrivalDepartureDate.time - predictionDate.time
        val time2 = other.arrivalDepartureDate.time - other.predictionDate.time
        return time1.compareTo(time2)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeParcelable(trainStation, flags)
            writeParcelable(stop, flags)
            writeString(routeName.toTextString())
            writeString(destName)
            writeLong(predictionDate.time)
            writeLong(arrivalDepartureDate.time)
            writeString(isApp.toString())
            writeString(isDly.toString())
        }
    }

    companion object {

        private const val serialVersionUID = 0L

        fun buildFakeEtaWith(trainStation: TrainStation, arrivalDepartureDate: Date, predictionDate: Date, app: Boolean, delay: Boolean): TrainEta {
            return TrainEta(trainStation, Stop.buildEmptyStop(), TrainLine.NA, StringUtils.EMPTY, predictionDate, arrivalDepartureDate, app, delay)
        }

        @JvmField
        val CREATOR: Parcelable.Creator<TrainEta> = object : Parcelable.Creator<TrainEta> {
            override fun createFromParcel(source: Parcel): TrainEta {
                return TrainEta(source)
            }

            override fun newArray(size: Int): Array<TrainEta?> {
                return arrayOfNulls(size)
            }
        }
    }
}
