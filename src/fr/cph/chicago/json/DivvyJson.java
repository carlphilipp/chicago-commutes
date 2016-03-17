package fr.cph.chicago.json;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import fr.cph.chicago.entity.BikeStation;

public class DivvyJson {
    @JsonProperty("executionTime")
    private String executionTime;
    @JsonProperty("stationBeanList")
    private List<BikeStation> stations;

    public DivvyJson() {
    }

    @NonNull
    public List<BikeStation> getStations() {
        return stations;
    }
}