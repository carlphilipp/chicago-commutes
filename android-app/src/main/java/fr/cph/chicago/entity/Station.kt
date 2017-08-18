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
import fr.cph.chicago.entity.enumeration.TrainLine
import org.apache.commons.lang3.StringUtils
import java.util.*

/**
 * Station entity

 * @author Carl-Philipp Harmant
 * *
 * @version 1
 */
class Station(
    var id: Int,
    var name: String,
    var stops: MutableList<Stop>) : Comparable<Station>, Parcelable, AStation {

    private constructor(source: Parcel) : this(
        id = source.readInt(),
        name = source.readString(),
        stops = source.createTypedArrayList(Stop.CREATOR))

    val lines: Set<TrainLine>
        get() {
            return stops
                .map { it.lines }
                .fold(TreeSet(), { accumulator, item -> accumulator.addAll(item); accumulator })
        }

    val stopByLines: Map<TrainLine, MutableList<Stop>>
        get() {
            val result = TreeMap<TrainLine, MutableList<Stop>>()
            for (stop in stops) {
                for (trainLine in stop.lines) {
                    if (result.containsKey(trainLine)) {
                        result[trainLine]!!.add(stop)
                    } else {
                        result.put(trainLine, mutableListOf(stop))
                    }
                }
            }
            return result
        }

    override fun compareTo(other: Station): Int {
        return this.name.compareTo(other.name)
    }

    val stopsPosition: List<Position> get() = stops.map { it.position }.toMutableList()

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(name)
        dest.writeTypedList(stops)
    }

    companion object {

        fun buildEmptyStation(): Station {
            return Station(0, StringUtils.EMPTY, mutableListOf())
        }

        @JvmField
        val CREATOR: Parcelable.Creator<Station> = object : Parcelable.Creator<Station> {
            override fun createFromParcel(source: Parcel): Station {
                return Station(source)
            }

            override fun newArray(size: Int): Array<Station?> {
                return arrayOfNulls(size)
            }
        }
    }
}
