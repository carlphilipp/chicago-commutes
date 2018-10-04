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

/**
 * Bus route entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
data class BusRoute(val id: String, val name: String) : Parcelable {

    private constructor(source: Parcel) : this(
        id = source.readString() ?: "",
        name = source.readString() ?: ""
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<BusRoute> = object : Parcelable.Creator<BusRoute> {
            override fun createFromParcel(source: Parcel): BusRoute {
                return BusRoute(source)
            }

            override fun newArray(size: Int): Array<BusRoute?> {
                return arrayOfNulls(size)
            }
        }
    }
}
