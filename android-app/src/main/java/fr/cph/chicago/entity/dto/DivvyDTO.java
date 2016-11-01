package fr.cph.chicago.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import fr.cph.chicago.entity.BikeStation;
import lombok.Data;


@Data
public class DivvyDTO {

    @JsonProperty("executionTime")
    private String executionTime;
    @JsonProperty("stationBeanList")
    private List<BikeStation> stations;

    public DivvyDTO() {
    }
}
