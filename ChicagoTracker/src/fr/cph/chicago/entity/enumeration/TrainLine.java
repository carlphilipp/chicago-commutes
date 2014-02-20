/**
 * Copyright 2014 Carl-Philipp Harmant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.entity.enumeration;

import android.graphics.Color;

/**
 * 
 * @author carl
 * 
 */
public enum TrainLine {
	BLUE("Blue", "Blue", "Blue line", Color.rgb(0, 0, 204)), BROWN("Brn", "Brown", "Brown line", Color.rgb(102, 51, 0)), GREEN("G", "Green",
			"Green line", Color.rgb(0, 153, 0)), ORANGE("Org", "Orange", "Orange line", Color.rgb(255, 128, 0)), PINK("Pink", "Pink", "Pink line",
			Color.rgb(204, 0, 102)), PURPLE("P", "Purple", "Purple line", Color.rgb(102, 0, 102)),
	// PURPLE_EXPRESS("Pexp", "Purple Express","Purple line express", Color.rgb(102, 0, 102)),
	RED("Red", "Red", "Red line", Color.rgb(240, 0, 0)), YELLOW("Y", "Yellow", "Yellow line", Color.rgb(255, 255, 0));

	/** **/
	private String text;
	/** **/
	private String name;
	/** **/
	private String withLine;
	/** **/
	private int color;

	/**
	 * 
	 * @param text
	 * @param name
	 * @param withLine
	 * @param color
	 */
	private TrainLine(final String text, final String name, final String withLine, final int color) {
		this.text = text;
		this.name = name;
		this.withLine = withLine;
		this.color = color;
	}

	/**
	 * 
	 * @param text
	 * @return
	 */
	public static final TrainLine fromXmlString(final String text) {
		if (text != null) {
			for (TrainLine b : TrainLine.values()) {
				if (text.equalsIgnoreCase(b.text)) {
					return b;
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param text
	 * @return
	 */
	public static final TrainLine fromString(final String text) {
		if (text != null) {
			for (TrainLine b : TrainLine.values()) {
				if (text.equalsIgnoreCase(b.name)) {
					return b;
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @return
	 */
	public final String toStringWithLine() {
		return withLine;
	}

	/**
	 * 
	 * @return
	 */
	public final int getColor() {
		return color;
	}

	@Override
	public final String toString() {
		return name;
	}
}
