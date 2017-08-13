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
import java.util.*

/**
 * Train Arrival entity

 * @author Carl-Philipp Harmant
 * *
 * @version 1
 */
class TrainArrival(
    val timeStamp: Date? = null,
    val errorCode: Int = 0,
    val errorMessage: String? = null,
    var etas: List<Eta>? = null) : Parcelable {

    private constructor(`in`: Parcel) : this() {
        readFromParcel(`in`)
    }

    fun getEtas(line: TrainLine): List<Eta> {
        val result = ArrayList<Eta>()
        if (this.etas != null) {
            // FIXME kotlin
/*            result.addAll(
                Stream.of(this.etas!!)
                    .filter { eta -> eta.routeName === line }
                    .collect<List<Eta>, Any>(Collectors.toList<Eta>())
            )*/
        }
        return result
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        //		dest.writeLong(timeStamp.getTime());
        //		dest.writeInt(errorCode);
        //		dest.writeString(errorMessage);
        dest.writeList(etas)
    }

    private fun readFromParcel(`in`: Parcel) {
        //		timeStamp = new Date(in.readLong());
        //		errorCode = in.readInt();
        //		errorMessage = in.readString();
        etas = ArrayList<Eta>()
        `in`.readList(etas, Eta::class.java.classLoader)
    }

    companion object {

        val CREATOR: Parcelable.Creator<TrainArrival> = object : Parcelable.Creator<TrainArrival> {
            override fun createFromParcel(`in`: Parcel): TrainArrival {
                return TrainArrival(`in`)
            }

            override fun newArray(size: Int): Array<TrainArrival> {
                // FIXME kotlin
                return arrayOf()
            }
        }
    }
}
