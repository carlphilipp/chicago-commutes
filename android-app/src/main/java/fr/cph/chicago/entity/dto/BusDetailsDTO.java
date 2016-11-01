package fr.cph.chicago.entity.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BusDetailsDTO {
    private String busRouteId;
    private String bound;
    private String boundTitle;
    private String stopId;
    private String routeName;
    private String stopName;
}
