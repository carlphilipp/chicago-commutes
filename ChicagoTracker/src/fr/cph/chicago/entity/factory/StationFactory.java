package fr.cph.chicago.entity.factory;

import java.util.List;

import fr.cph.chicago.entity.Station;
import fr.cph.chicago.entity.Stop;

public class StationFactory {
	private StationFactory() {

	}

	public static Station buildStation(final Integer id, final String name, final List<Stop> stops) {
		Station station = new Station();
		station.setId(id);
		station.setName(name);
		station.setStops(stops);
		return station;
	}
}
