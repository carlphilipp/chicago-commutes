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

package fr.cph.chicago.entity

import android.os.Parcel
import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.apache.commons.lang3.StringUtils
import java.io.Serializable

/**
 * Bus stop entity. This can't be immutable because it needs to extends RealmObject.

 * @author Carl-Philipp Harmant
 * *
 * @version 1
 */
open class BusStop(
    @PrimaryKey
    var id: Int = 0,
    var name: String = StringUtils.EMPTY,
    var description: String = StringUtils.EMPTY,
    var position: Position? = null) : RealmObject(), Comparable<BusStop>, Parcelable, Serializable, AStation {

    private constructor(source: Parcel) : this(
        id = source.readInt(),
        name = source.readString(),
        description = source.readString(),
        position = source.readParcelable<Position>(Position::class.java.classLoader))

    override fun toString(): String {
        return "[id:$id;name:$name;description:$description;position:$position]"
    }

    override fun compareTo(other: BusStop): Int {
        val position = other.position
        val latitude = java.lang.Double.compare(position!!.latitude, position.latitude)
        return if (latitude == 0) java.lang.Double.compare(position.longitude, position.longitude) else latitude
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

    companion object {

        private const val serialVersionUID = 0L

        @JvmField val CREATOR: Parcelable.Creator<BusStop> = object : Parcelable.Creator<BusStop> {
            override fun createFromParcel(source: Parcel): BusStop {
                return BusStop(source)
            }

            override fun newArray(size: Int): Array<BusStop?> {
                return arrayOfNulls(size)
            }
        }
    }
}
