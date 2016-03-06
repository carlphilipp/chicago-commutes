package fr.cph.chicago.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.cph.chicago.entity.BikeStation;

import java.util.List;

public class DivvyJson {
	@JsonProperty("executionTime")
	private String executionTime;
	@JsonProperty("stationBeanList")
	private List<BikeStation> stations;

	public DivvyJson() {
	}

	public List<BikeStation> getStations() {
		return stations;
	}
}