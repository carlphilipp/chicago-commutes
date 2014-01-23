package fr.cph.chicago.entity.enumeration;

public enum PredictionType {
	ARRIVAL("A"), DEPARTURE("D");

	private String message;

	private PredictionType(String message) {
		this.message = message;
	}

	public static PredictionType fromString(String text) {
		if (text != null) {
			for (PredictionType b : PredictionType.values()) {
				if (text.equalsIgnoreCase(b.message)) {
					return b;
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return this.message;
	}
}
