package fr.cph.chicago.entity.dto;

import android.util.SparseArray;
import fr.cph.chicago.entity.TrainArrival;
import lombok.Builder;
import lombok.Data;

/**
 * @author cpharmant
 */
@Builder
@Data
public class TrainArrivalDTO {
    private boolean error;
    private SparseArray<TrainArrival> trainArrivalSparseArray;
}
