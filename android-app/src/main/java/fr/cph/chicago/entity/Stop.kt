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
import fr.cph.chicago.entity.enumeration.TrainDirection
import fr.cph.chicago.entity.enumeration.TrainLine
import java.util.*

/**
 * Station entity

 * @author Carl-Philipp Harmant
 * *
 * @version 1
 */
class Stop(
    var id: Int = 0,
    var description: String? = null,
    var direction: TrainDirection? = null,
    var position: Position? = null,
    var ada: Boolean = false,
    var lines: MutableList<TrainLine>? = null) : Comparable<Stop>, Parcelable {

    private constructor(`in`: Parcel) : this()

    /*init {
        readFromParcel(`in`)
    }*/

    override fun compareTo(anotherStop: Stop): Int {
        return this.direction!!.compareTo(anotherStop.direction)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(description)
        dest.writeString(direction!!.toTextString())
        dest.writeParcelable(position, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
        dest.writeString(ada.toString())
        // FIXME kotlin
        //val linesString = Stream.of(lines!!).map<String>(Function<TrainLine, String> { it.toTextString() }).collect<List<String>, Any>(Collectors.toList<String>())
        //dest.writeStringList(linesString)
    }

    private fun readFromParcel(`in`: Parcel) {
        id = `in`.readInt()
        description = `in`.readString()
        direction = TrainDirection.fromString(`in`.readString())
        position = `in`.readParcelable<Position>(Position::class.java.classLoader)
        ada = java.lang.Boolean.valueOf(`in`.readString())!!
        val linesString = ArrayList<String>()
        `in`.readStringList(linesString)
        lines = ArrayList<TrainLine>()
        // FIXME kotlin
        //lines!!.addAll(Stream.of(linesString).map<TrainLine>(Function<String, TrainLine> { TrainLine.fromXmlString(it) }).collect<List<TrainLine>, Any>(Collectors.toList<TrainLine>()))
    }

    companion object {

        val CREATOR: Parcelable.Creator<Stop> = object : Parcelable.Creator<Stop> {
            override fun createFromParcel(`in`: Parcel): Stop {
                return Stop(`in`)
            }

            override fun newArray(size: Int): Array<Stop> {
                // FIXME kotlin
                //return arrayOfNulls(size)
                return arrayOf()
            }
        }
    }
}
