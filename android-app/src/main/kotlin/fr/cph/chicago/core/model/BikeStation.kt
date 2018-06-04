package fr.cph.chicago.core.model

import android.os.Parcel
import android.os.Parcelable
import org.apache.commons.lang3.StringUtils

data class BikeStation(
    val id: Int,
    val name: String,
    val availableDocks: Int,
    val availableBikes: Int,
    val latitude: Double,
    val longitude: Double,
    val address: String) : Parcelable, AStation {

    private constructor(source: Parcel) : this(
        id = source.readInt(),
        name = source.readString(),
        availableDocks = source.readInt(),
        availableBikes = source.readInt(),
        latitude = source.readDouble(),
        longitude = source.readDouble(),
        address = source.readString())

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

    companion object {

        private const val DEFAULT_RANGE = 0.008

        fun buildDefaultBikeStationWithName(name: String): BikeStation {
            return BikeStation(0, name, -1, -1, 0.0, 0.0, StringUtils.EMPTY)
        }

        fun readNearbyStation(divvyStations: List<BikeStation>, position: Position): List<BikeStation> {
            val latitude = position.latitude
            val longitude = position.longitude

            val latMax = latitude + DEFAULT_RANGE
            val latMin = latitude - DEFAULT_RANGE
            val lonMax = longitude + DEFAULT_RANGE
            val lonMin = longitude - DEFAULT_RANGE

            return divvyStations
                .filter { station -> station.latitude <= latMax }
                .filter { station -> station.latitude >= latMin }
                .filter { station -> station.longitude <= lonMax }
                .filter { station -> station.longitude >= lonMin }
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
}
