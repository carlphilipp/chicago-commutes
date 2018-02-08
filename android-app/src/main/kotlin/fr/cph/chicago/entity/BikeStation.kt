/**
 * Copyright 2018 Carl-Philipp Harmant
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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.commons.lang3.StringUtils

/**
 * Bike station entity

 * @author Carl-Philipp Harmant
 * *
 * @version 1
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class BikeStation(
    @JsonProperty("id")
    val id: Int,
    @JsonProperty("stationName")
    val name: String,
    @JsonProperty("availableDocks")
    val availableDocks: Int,
    @JsonProperty("totalDocks")
    val totalDocks: Int,
    @JsonProperty("latitude")
    val latitude: Double,
    @JsonProperty("longitude")
    val longitude: Double,
    @JsonProperty("availableBikes")
    val availableBikes: Int,
    @JsonProperty("stAddress1")
    val stAddress1: String) : Parcelable, AStation {

    private constructor(source: Parcel) : this(
        id = source.readInt(),
        name = source.readString(),
        availableDocks = source.readInt(),
        totalDocks = source.readInt(),
        latitude = source.readDouble(),
        longitude = source.readDouble(),
        availableBikes = source.readInt(),
        stAddress1 = source.readString())

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(name)
        dest.writeInt(availableDocks)
        dest.writeInt(totalDocks)
        dest.writeDouble(latitude)
        dest.writeDouble(longitude)
        dest.writeInt(availableBikes)
        dest.writeString(stAddress1)
    }

    companion object {

        private val DEFAULT_RANGE = 0.008

        fun buildDefaultBikeStationWithName(name: String): BikeStation {
            return BikeStation(0, name, -1, 0, 0.0, 0.0, -1, StringUtils.EMPTY)
        }

        fun readNearbyStation(bikeStations: List<BikeStation>, position: Position): List<BikeStation> {
            val latitude = position.latitude
            val longitude = position.longitude

            val latMax = latitude + DEFAULT_RANGE
            val latMin = latitude - DEFAULT_RANGE
            val lonMax = longitude + DEFAULT_RANGE
            val lonMin = longitude - DEFAULT_RANGE

            return bikeStations
                .filter { station -> station.latitude <= latMax }
                .filter { station -> station.latitude >= latMin }
                .filter { station -> station.longitude <= lonMax }
                .filter { station -> station.longitude >= lonMin }
                .toList()
        }

        @JvmField
        val CREATOR: Parcelable.Creator<BikeStation> = object : Parcelable.Creator<BikeStation> {
            override fun createFromParcel(source: Parcel): BikeStation {
                return BikeStation(source)
            }

            override fun newArray(size: Int): Array<BikeStation?> {
                return arrayOfNulls(size)
            }
        }
    }
}
