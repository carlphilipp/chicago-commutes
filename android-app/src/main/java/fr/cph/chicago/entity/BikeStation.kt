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
import com.annimon.stream.Collectors
import com.annimon.stream.Stream
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
class BikeStation(
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
        source.readInt(),
        source.readString(),
        source.readInt(),
        source.readInt(),
        source.readDouble(),
        source.readDouble(),
        source.readInt(),
        source.readString())

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

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (javaClass != other.javaClass)
            return false
        val otherCast = other as BikeStation?
        return id == otherCast!!.id
    }

    override fun hashCode(): Int {
        var result: Int
        var temp: Long
        result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + availableDocks
        result = 31 * result + totalDocks
        temp = java.lang.Double.doubleToLongBits(latitude)
        result = 31 * result + (temp xor temp.ushr(32)).toInt()
        temp = java.lang.Double.doubleToLongBits(longitude)
        result = 31 * result + (temp xor temp.ushr(32)).toInt()
        result = 31 * result + availableBikes
        result = 31 * result + stAddress1.hashCode()
        return result
    }

    companion object {

        private val DEFAULT_RANGE = 0.008

        fun buildDefaultBikeStationWithName(name: String): BikeStation {
            return BikeStation(0, name, 0, 0, 0.0, 0.0, 0, StringUtils.EMPTY)
        }

        fun readNearbyStation(bikeStations: List<BikeStation>, position: Position): List<BikeStation> {
            val latitude = position.latitude
            val longitude = position.longitude

            val latMax = latitude + DEFAULT_RANGE
            val latMin = latitude - DEFAULT_RANGE
            val lonMax = longitude + DEFAULT_RANGE
            val lonMin = longitude - DEFAULT_RANGE

            return Stream.of(bikeStations)
                .filter { station -> station.latitude <= latMax }
                .filter { station -> station.latitude >= latMin }
                .filter { station -> station.longitude <= lonMax }
                .filter { station -> station.longitude >= lonMin }
                .collect(Collectors.toList())
        }

        @JvmField val CREATOR: Parcelable.Creator<BikeStation> = object : Parcelable.Creator<BikeStation> {
            override fun createFromParcel(source: Parcel): BikeStation {
                return BikeStation(source)
            }

            override fun newArray(size: Int): Array<BikeStation?> {
                return arrayOfNulls(size)
            }
        }
    }
}
