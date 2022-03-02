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

/**
 * Bus stop entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class BusStop(
    id: String,
    name: String,
    val description: String,
    val position: Position) : Comparable<BusStop>, Parcelable, Station(id, name) {

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<BusStop> = object : Parcelable.Creator<BusStop> {
            override fun createFromParcel(source: Parcel): BusStop {
                return BusStop(source)
            }

            override fun newArray(size: Int): Array<BusStop?> {
                return arrayOfNulls(size)
            }
        }
    }

    private constructor(source: Parcel) : this(
        id = source.readString() ?: "",
        name = source.readString() ?: "",
        description = source.readString() ?: "",
        position = source.readParcelable<Position>(Position::class.java.classLoader) ?: Position())

    override fun toString(): String {
        return "[id:$id;name:$name;description:$description;position:$position]"
    }

    override fun compareTo(other: BusStop): Int {
        val position = other.position
        val latitude = position.latitude.compareTo(position.latitude)
        return if (latitude == 0) position.longitude.compareTo(position.longitude) else latitude
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeString(description)
        dest.writeParcelable(position, flags)
    }
}
