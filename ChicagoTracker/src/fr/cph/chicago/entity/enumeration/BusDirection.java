/**
 * Copyright 2016 Carl-Philipp Harmant
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.entity.enumeration;

import java.util.Locale;

/**
 * Enumeration, bus direction
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public enum BusDirection {

	NORTHBOUND("Northbound"), WESTBOUND("Westbound"), SOUTHBOUND("Southbound"), EASTBOUND("Eastbound");

	/** The text **/
	private String text;

	/**
	 * Private constructor
	 *
	 * @param text
	 *            the text
	 */
	BusDirection(String text) {
		this.text = text;
	}

	/**
	 * Get bus direction from string
	 *
	 * @param textthe
	 *            text
	 * @return a bus direction
	 */
	public static BusDirection fromString(final String text) {
		if (text != null) {
			for (final BusDirection b : BusDirection.values()) {
				if (text.equalsIgnoreCase(b.text)) {
					return b;
				} else if (b.text.toLowerCase(Locale.US).contains(text.toLowerCase(Locale.US))) {
					return b;
				} else if (b.text.toLowerCase(Locale.US).contains(text.toLowerCase(Locale.US))) {
					return b;
				}
			}
		}
		return null;
	}

	@Override
	public final String toString() {
		return text;
	}
}
