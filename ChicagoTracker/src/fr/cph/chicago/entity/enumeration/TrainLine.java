package fr.cph.chicago.entity.enumeration;

import android.graphics.Color;

public enum TrainLine {
	BLUE("Blue", "Blue", "Blue line", Color.rgb(0, 0, 204)), 
	BROWN("Brn", "Brown", "Brown line", Color.rgb(102, 51, 0)), 
	GREEN("G", "Green", "Green line", Color.rgb(0, 153, 0)), 
	ORANGE("Org", "Orange", "Orange line", Color.rgb(255, 128, 0)), 
	PINK("Pink", "Pink", "Pink line",Color.rgb(204, 0, 102)), 
	PURPLE("P", "Purple", "Purple line", Color.rgb(102, 0, 102)),
//	PURPLE_EXPRESS("Pexp", "Purple Express","Purple line express", Color.rgb(102, 0, 102)), 	
	RED("Red", "Red", "Red line", Color.rgb(240, 0, 0)), 
	YELLOW("Y", "Yellow", "Yellow line",Color.rgb(255, 255, 0));

	private String text;
	private String name;
	private String withLine;
	private int color;

	TrainLine(String text, String name, String withLine, int color) {
		this.text = text;
		this.name = name;
		this.withLine = withLine;
		this.color = color;
	}

	public static TrainLine fromXmlString(String text) {
		if (text != null) {
			for (TrainLine b : TrainLine.values()) {
				if (text.equalsIgnoreCase(b.text)) {
					return b;
				}
			}
		}
		return null;
	}
	
	public static TrainLine fromString(String text) {
		if (text != null) {
			for (TrainLine b : TrainLine.values()) {
				if (text.equalsIgnoreCase(b.name)) {
					return b;
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return name;
	}

	public String toStringWithLine() {
		return withLine;
	}

	public int getColor() {
		return color;
	}
}
