package fr.cph.chicago.entity.enumeration;

public enum TrainDirection {
	NORTH("N", "North"),
	SOUTH("S", "South"),
	EAST("E", "East"), 
	WEST("W", "West");

	private String text;
	private String formattedText;

	TrainDirection(String text, String formattedText) {
		this.text = text;
		this.formattedText = formattedText;
	}

	public static TrainDirection fromString(String text) {
		if (text != null) {
			for (TrainDirection b : TrainDirection.values()) {
				if (text.equalsIgnoreCase(b.text)) {
					return b;
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return this.formattedText;
	}
}
