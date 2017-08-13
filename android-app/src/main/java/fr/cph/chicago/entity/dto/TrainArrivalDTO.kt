package fr.cph.chicago.entity.dto

import android.util.SparseArray
import fr.cph.chicago.entity.TrainArrival

/**
 * @author cpharmant
 */
class TrainArrivalDTO(val trainArrivalSparseArray: SparseArray<TrainArrival>? = null, val error: Boolean = false)
