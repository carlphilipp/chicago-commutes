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
import java.io.Serializable

/**
 * Bus route entity

 * @author Carl-Philipp Harmant
 * *
 * @version 1
 */
class BusRoute(var id: String? = null, var name: String? = null) : Parcelable, Serializable {

    private constructor(`in`: Parcel) : this() {
        readFromParcel(`in`)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
    }

    private fun readFromParcel(`in`: Parcel) {
        id = `in`.readString()
        name = `in`.readString()
    }

    companion object {

        private const val serialVersionUID = 0L

        val CREATOR: Parcelable.Creator<BusRoute> = object : Parcelable.Creator<BusRoute> {
            override fun createFromParcel(`in`: Parcel): BusRoute {
                return BusRoute(`in`)
            }

            override fun newArray(size: Int): Array<BusRoute> {
                // FIXME parcelable kotlin
                return arrayOf()
            }
        }
    }
}
