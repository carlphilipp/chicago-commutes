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
import java.util.*

/**
 * @author carl
 */
class BusPattern() : Parcelable {

    var id: Int = 0
    var length: Double = 0.toDouble()
    var direction: String? = null
    var points: MutableList<PatternPoint>? = null

    private constructor(`in`: Parcel) : this() {
        readFromParcel(`in`)
    }

    fun addPoint(patternPoint: PatternPoint) {
        if (points == null) {
            points = ArrayList<PatternPoint>()
        }
        this.points!!.add(patternPoint)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeDouble(length)
        dest.writeString(direction)
        dest.writeList(points)
    }

    private fun readFromParcel(`in`: Parcel) {
        id = `in`.readInt()
        length = `in`.readDouble()
        direction = `in`.readString()
        `in`.readList(points, PatternPoint::class.java.classLoader)
    }

    companion object {

        val CREATOR: Parcelable.Creator<BusPattern> = object : Parcelable.Creator<BusPattern> {
            override fun createFromParcel(`in`: Parcel): BusPattern {
                return BusPattern(`in`)
            }

            override fun newArray(size: Int): Array<BusPattern> {
                // FIXME kotlin
                return arrayOf()
            }
        }
    }
}
