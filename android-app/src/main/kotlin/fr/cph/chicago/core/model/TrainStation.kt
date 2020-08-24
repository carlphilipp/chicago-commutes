/**
 * Copyright 2020 Carl-Philipp Harmant
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
import fr.cph.chicago.core.model.enumeration.TrainLine
import java.math.BigInteger
import java.util.TreeMap
import java.util.TreeSet
import org.apache.commons.lang3.StringUtils

/**
 * Train station entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
class TrainStation(
    id: BigInteger,
    name: String,
    var stops: List<Stop>) : Comparable<TrainStation>, Parcelable, Station(id, name) {

    companion object {
        fun buildEmptyStation(): TrainStation {
            return TrainStation(BigInteger.ZERO, StringUtils.EMPTY, mutableListOf())
        }

        @JvmField
        val CREATOR: Parcelable.Creator<TrainStation> = object : Parcelable.Creator<TrainStation> {
            override fun createFromParcel(source: Parcel): TrainStation {
                return TrainStation(source)
            }

            override fun newArray(size: Int): Array<TrainStation?> {
                return arrayOfNulls(size)
            }
        }
    }

    private constructor(source: Parcel) : this(
        id = BigInteger(source.readString()!!),
        name = source.readString() ?: StringUtils.EMPTY,
        stops = source.createTypedArrayList(Stop.CREATOR) ?: listOf())

    val lines: Set<TrainLine>
        get() {
            return stops
                .map { it.lines }
                .fold(TreeSet()) { accumulator, item -> accumulator.addAll(item); accumulator }
        }

    val stopByLines: Map<TrainLine, List<Stop>>
        get() {
            val result = TreeMap<TrainLine, MutableList<Stop>>()
            for (stop in stops) {
                for (trainLine in stop.lines) {
                    if (result.contains(trainLine)) {
                        result.getValue(trainLine).add(stop)
                    } else {
                        result[trainLine] = mutableListOf(stop)
                    }
                }
            }
            return result
        }

    override fun compareTo(other: TrainStation): Int {
        return this.name.compareTo(other.name)
    }

    val stopsPosition: List<Position> get() = stops.map { it.position }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id.toString())
        dest.writeString(name)
        dest.writeTypedList(stops)
    }

    override fun toString(): String {
        return "TrainStation(stops=$stops)"
    }
}
