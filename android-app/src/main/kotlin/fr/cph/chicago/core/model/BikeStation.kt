package fr.cph.chicago.core.model

import android.os.Parcel
import android.os.Parcelable
import org.apache.commons.lang3.StringUtils

class BikeStation(
    id: Int,
    name: String,
    val availableDocks: Int,
    val availableBikes: Int,
    val latitude: Double,
    val longitude: Double,
    val address: String) : Parcelable, Station(id, name) {

    companion object {
        fun buildUnknownStation(): BikeStation {
            return buildDefaultBikeStationWithName("Unknown")
        }

        fun buildDefaultBikeStationWithName(name: String, id : Int = 0): BikeStation {
            return BikeStation(id, name, -1, -1, 0.0, 0.0, StringUtils.EMPTY)
        }

        @JvmField
        val CREATOR: Parcelable.Creator<BikeStation> = object : Parcelable.Creator<BikeStation> {
            override fun createFromParcel(source: Parcel): BikeStation {
                return BikeStation(source)
            }

            override fun newArray(size: Int): Array<BikeStation?> {
                return arrayOfNulls(size)
            }
        }
    }

    private constructor(source: Parcel) : this(
        id = source.readInt(),
        name = source.readString() ?: StringUtils.EMPTY,
        availableDocks = source.readInt(),
        availableBikes = source.readInt(),
        latitude = source.readDouble(),
        longitude = source.readDouble(),
        address = source.readString() ?: StringUtils.EMPTY)

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(name)
        dest.writeInt(availableDocks)
        dest.writeInt(availableBikes)
        dest.writeDouble(latitude)
        dest.writeDouble(longitude)
        dest.writeString(address)
    }
}
