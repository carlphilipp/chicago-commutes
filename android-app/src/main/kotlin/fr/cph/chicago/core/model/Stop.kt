/**
 * Copyright 2019 Carl-Philipp Harmant
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
import fr.cph.chicago.core.model.enumeration.TrainDirection
import fr.cph.chicago.core.model.enumeration.TrainLine
import org.apache.commons.lang3.StringUtils

/**
 * Stop entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
data class Stop(
    var id: Int,
    var description: String,
    var direction: TrainDirection,
    var position: Position,
    var ada: Boolean,
    var lines: Set<TrainLine>) : Comparable<Stop>, Parcelable {

    companion object {
        fun buildEmptyStop(): Stop {
            return Stop(0, StringUtils.EMPTY, TrainDirection.UNKNOWN, Position(), false, setOf())
        }

        @JvmField
        val CREATOR: Parcelable.Creator<Stop> = object : Parcelable.Creator<Stop> {
            override fun createFromParcel(source: Parcel): Stop {
                return Stop(source)
            }

            override fun newArray(size: Int): Array<Stop?> {
                return arrayOfNulls(size)
            }
        }
    }

    private constructor(source: Parcel) : this(
        id = source.readInt(),
        description = source.readString() ?: StringUtils.EMPTY,
        direction = TrainDirection.fromString(source.readString() ?: StringUtils.EMPTY),
        position = source.readParcelable<Position>(Position::class.java.classLoader) ?: Position(),
        ada = source.readString()!!.toBoolean(),
        lines = source.createStringArrayList()?.map { TrainLine.fromXmlString(it) }?.toSet()
            ?: setOf()
    )

    override fun compareTo(other: Stop): Int {
        return this.direction.compareTo(other.direction)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(description)
        dest.writeString(direction.toTextString())
        dest.writeParcelable(position, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
        dest.writeString(ada.toString())
        val linesString = lines.map { it.toTextString() }
        dest.writeStringList(linesString)
    }
}
