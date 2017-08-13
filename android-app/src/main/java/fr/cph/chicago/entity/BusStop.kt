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
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable

/**
 * Bus stop entity

 * @author Carl-Philipp Harmant
 * *
 * @version 1
 */
open class BusStop : RealmObject, Comparable<BusStop>, Parcelable, Serializable, AStation {
    @PrimaryKey
    var id: Int = 0
    var name: String? = null
    var description: String? = null
    var position: Position? = null

    constructor() {}

    private constructor(`in`: Parcel) {
        readFromParcel(`in`)
    }

    override fun toString(): String {
        return "[id:" + id + ";name:" + name + ";position:" + position + "]"
    }

    override fun compareTo(another: BusStop): Int {
        val position = another.position
        val latitude = java.lang.Double.compare(position!!.latitude, position!!.latitude)
        return if (latitude == 0) java.lang.Double.compare(position!!.longitude, position!!.longitude) else latitude
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(name)
        dest.writeString(description)
        dest.writeParcelable(position, flags)
    }

    private fun readFromParcel(`in`: Parcel) {
        id = `in`.readInt()
        name = `in`.readString()
        description = `in`.readString()
        position = `in`.readParcelable<Position>(Position::class.java.classLoader)
    }

    companion object {

        private const val serialVersionUID = 0L

        val CREATOR: Parcelable.Creator<BusStop> = object : Parcelable.Creator<BusStop> {
            override fun createFromParcel(`in`: Parcel): BusStop {
                return BusStop(`in`)
            }

            override fun newArray(size: Int): Array<BusStop> {
                // FIXME parcelable kotlin
                return arrayOf()
            }
        }
    }
}
