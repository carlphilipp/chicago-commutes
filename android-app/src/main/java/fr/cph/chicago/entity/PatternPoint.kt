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

class PatternPoint() : Parcelable {
    /**
     * Sequence number
     */
    var sequence: Int = 0
    /**
     * The position
     */
    var position: Position? = null
    /**
     * The type
     */
    var type: String? = null
    /**
     * The stop id
     */
    var stopId: Int = 0
    /**
     * The stop name
     */
    var stopName: String? = null
    /**
     * The distance
     */
    var distance: Double = 0.toDouble()

    private constructor(`in`: Parcel) : this() {
        readFromParcel(`in`)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(sequence)
        dest.writeParcelable(position, flags)
        dest.writeString(type)
        dest.writeInt(stopId)
        dest.writeString(stopName)
        dest.writeDouble(distance)
    }

    private fun readFromParcel(`in`: Parcel) {
        sequence = `in`.readInt()
        position = `in`.readParcelable<Position>(Position::class.java.classLoader)
        type = `in`.readString()
        stopId = `in`.readInt()
        stopName = `in`.readString()
        distance = `in`.readDouble()
    }

    companion object {

        val CREATOR: Parcelable.Creator<PatternPoint> = object : Parcelable.Creator<PatternPoint> {
            override fun createFromParcel(`in`: Parcel): PatternPoint {
                return PatternPoint(`in`)
            }

            override fun newArray(size: Int): Array<PatternPoint> {
                // FIXME kotlin
                return arrayOf()
            }
        }
    }
}
