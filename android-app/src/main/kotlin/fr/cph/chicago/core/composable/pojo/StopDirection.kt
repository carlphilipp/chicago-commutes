package fr.cph.chicago.core.composable.pojo

import fr.cph.chicago.core.model.enumeration.TrainDirection

data class StopDirection(val destination: String, val trainDirection: TrainDirection) : java.lang.Comparable<StopDirection> {
    override fun compareTo(stopDirection: StopDirection): Int {
        if (this == stopDirection) {
            return 0;
        }
        val dest = this.destination.compareTo(stopDirection.destination)
        return if (dest == 0) {
            this.trainDirection.compareTo(stopDirection.trainDirection)
        } else {
            dest
        }
    }

}
