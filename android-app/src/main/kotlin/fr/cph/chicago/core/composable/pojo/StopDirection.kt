package fr.cph.chicago.core.composable.pojo

import fr.cph.chicago.core.model.enumeration.TrainDirection

data class StopDirection(val destination: String, val trainDirection: TrainDirection) : Comparable<StopDirection> {
    override fun compareTo(other: StopDirection): Int {
        if (this == other) {
            return 0;
        }
        val dest = this.destination.compareTo(other.destination)
        return if (dest == 0) {
            this.trainDirection.compareTo(other.trainDirection)
        } else {
            dest
        }
    }

}
