package fr.cph.chicago.entity.dto;

import fr.cph.chicago.entity.BusArrival;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author cpharmant
 */
@Builder
@Data
public class BusArrivalDTO {
    private boolean error;
    private List<BusArrival> busArrivals;
}
