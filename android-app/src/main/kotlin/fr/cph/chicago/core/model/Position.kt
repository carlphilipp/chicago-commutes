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
 * The position
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class Position(val latitude: Double = 0.0, val longitude: Double = 0.0) : Parcelable {

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Position> = object : Parcelable.Creator<Position> {
            override fun createFromParcel(source: Parcel): Position {
                return Position(source)
            }

            override fun newArray(size: Int): Array<Position?> {
                return arrayOfNulls(size)
            }
        }
    }

    private constructor(source: Parcel) : this(
        latitude = source.readDouble(),
        longitude = source.readDouble())

    override fun toString(): String {
        return "[latitude=$latitude;longitude=$longitude]"
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeDouble(latitude)
        dest.writeDouble(longitude)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Position

        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false

        return true
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        return result
    }

}
