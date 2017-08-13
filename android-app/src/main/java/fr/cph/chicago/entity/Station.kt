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
import com.annimon.stream.Collectors
import com.annimon.stream.Stream
import fr.cph.chicago.collector.CommutesCollectors
import fr.cph.chicago.entity.enumeration.TrainLine
import java.util.*
import kotlin.collections.ArrayList

/**
 * Station entity

 * @author Carl-Philipp Harmant
 * *
 * @version 1
 */
class Station(
    var id: Int = 0,
    var name: String? = null,
    var stops: List<Stop>? = null) : Comparable<Station>, Parcelable, AStation {

    private constructor(`in`: Parcel) : this() {
        readFromParcel(`in`)
    }

    val lines: Set<TrainLine>
        get() {
            return Stream.of(stops!!)
                .map { it.lines }
                .collect(CommutesCollectors.toTrainLineCollector())
        }

    val stopByLines: Map<TrainLine, List<Stop>>
        get() {
            val result = TreeMap<TrainLine, List<Stop>>()
            for (stop in stops!!) {
                val lines = stop.lines
                for (tl in lines!!) {
                    val stopss: MutableList<Stop> = mutableListOf()
                    if (result.containsKey(tl)) {
                        stopss.add(stop)
                    } else {
                        stopss.add(stop)
                        result.put(tl, stopss)
                    }
                }
            }
            return result
        }

    override fun compareTo(another: Station): Int {
        return this.name!!.compareTo(another.name!!)
    }

    val stopsPosition: List<Position?> get() = Stream.of(stops!!).map{ it.position }.collect(Collectors.toList()).toList()

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(name)
        dest.writeList(stops)
    }

    private fun readFromParcel(`in`: Parcel) {
        id = `in`.readInt()
        name = `in`.readString()
        stops = ArrayList<Stop>()
        `in`.readList(stops, Stop::class.java.classLoader)
    }

    companion object {

        val CREATOR: Parcelable.Creator<Station> = object : Parcelable.Creator<Station> {
            override fun createFromParcel(`in`: Parcel): Station {
                return Station(`in`)
            }

            override fun newArray(size: Int): Array<Station> {
                // FIXME parcelable kotlin
                return arrayOf()
            }
        }
    }
}
