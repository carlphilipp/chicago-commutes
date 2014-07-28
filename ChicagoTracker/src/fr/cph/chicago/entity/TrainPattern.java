package fr.cph.chicago.entity;

import java.util.List;

public class TrainPattern {
	private List<Position> positions;

	public final List<Position> getPositions() {
		return positions;
	}

	public final void setPositions(final List<Position> positions) {
		this.positions = positions;
	}
}
