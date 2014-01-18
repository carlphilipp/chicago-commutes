package fr.cph.chicago.entity.factory;

import fr.cph.chicago.entity.Stop;
import fr.cph.chicago.entity.enumeration.TrainDirection;

public final class StopFactory {
	private StopFactory() {
	}

	public static Stop buildStop(final Integer id, final String description, final TrainDirection direction) {
		Stop stop = new Stop();
		stop.setId(id);
		stop.setDescription(description);
		stop.setDirection(direction);
		return stop;
	}
}
