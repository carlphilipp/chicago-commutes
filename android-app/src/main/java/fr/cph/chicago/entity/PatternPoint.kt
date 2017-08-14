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

class PatternPoint(
    var position: Position,
    var type: String,
    var stopName: String? = null) : Parcelable {

    private constructor(source: Parcel) : this(
        source.readParcelable<Position>(Position::class.java.classLoader),
        source.readString(),
        source.readString())

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(position, flags)
        dest.writeString(type)
        dest.writeString(stopName)
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<PatternPoint> = object : Parcelable.Creator<PatternPoint> {
            override fun createFromParcel(source: Parcel): PatternPoint {
                return PatternPoint(source)
            }

            override fun newArray(size: Int): Array<PatternPoint?> {
                return arrayOfNulls(size)
            }
        }
    }
}
