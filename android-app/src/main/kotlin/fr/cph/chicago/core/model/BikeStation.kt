/**
 * Copyright 2021 Carl-Philipp Harmant
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
import java.util.Date

class BikeStation(
    id: String,
    name: String,
    val availableDocks: Int,
    val availableBikes: Int,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val lastReported: Date
) : Parcelable, Station(id, name) {

    companion object {
        const val DEFAULT_AVAILABLE = -1

        fun buildUnknownStation(): BikeStation {
            return buildDefaultBikeStationWithName(name = "Unknown")
        }

        fun buildDefaultBikeStationWithName(id: String = "", name: String): BikeStation {
            return BikeStation(id, name, DEFAULT_AVAILABLE, DEFAULT_AVAILABLE, 0.0, 0.0, "", Date())
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

    private constructor(source: Parcel) : this(
        id = source.readString() ?: "",
        name = source.readString() ?: "",
        availableDocks = source.readInt(),
        availableBikes = source.readInt(),
        latitude = source.readDouble(),
        longitude = source.readDouble(),
        address = source.readString() ?: "",
        lastReported = Date(source.readLong())
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeInt(availableDocks)
        dest.writeInt(availableBikes)
        dest.writeDouble(latitude)
        dest.writeDouble(longitude)
        dest.writeString(address)
        dest.writeLong(lastReported.time)
    }
}
