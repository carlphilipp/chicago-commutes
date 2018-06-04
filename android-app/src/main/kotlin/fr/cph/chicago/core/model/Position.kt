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

package fr.cph.chicago.core.model

import android.os.Parcel
import android.os.Parcelable
import io.realm.RealmObject
import java.io.Serializable

/**
 * The position. This can't be immutable because it needs to extends RealmObject.
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
open class Position(var latitude: Double = 0.0, var longitude: Double = 0.0) : RealmObject(), Parcelable, Serializable {

    private constructor(source: Parcel) : this() {
        readFromParcel(source)
    }

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

    private fun readFromParcel(source: Parcel) {
        latitude = source.readDouble()
        longitude = source.readDouble()
    }

    companion object {

        private const val serialVersionUID = 0L

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
}
