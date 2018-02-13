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
import fr.cph.chicago.entity.enumeration.TrainLine
import java.io.Serializable

/**
 * Train Arrival entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
data class TrainArrival(var etas: MutableList<Eta> = mutableListOf()) : Parcelable, Serializable {

    private constructor(source: Parcel) : this(etas = source.createTypedArray(Eta.CREATOR).toMutableList())

    fun getEtas(line: TrainLine): MutableList<Eta> {
        return this.etas.filter { eta -> eta.routeName == line }.toMutableList()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeTypedList(etas)
    }

    companion object {

        private const val serialVersionUID = 0L

        fun buildEmptyTrainArrival(): TrainArrival {
            return TrainArrival(mutableListOf())
        }

        @JvmField
        val CREATOR: Parcelable.Creator<TrainArrival> = object : Parcelable.Creator<TrainArrival> {
            override fun createFromParcel(source: Parcel): TrainArrival {
                return TrainArrival(source)
            }

            override fun newArray(size: Int): Array<TrainArrival?> {
                return arrayOfNulls(size)
            }
        }
    }
}
