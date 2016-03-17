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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Locale;

/**
 * Enumeration, bus direction
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */

public class BusDirection {

	private String textReceived;
	private BusDirectionEnum busDirectionEnum;

	public BusDirection(@NonNull final String textReceived) {
		this.textReceived = textReceived;
	}

	public boolean isOk() {
		final BusDirectionEnum en = BusDirectionEnum.fromString(textReceived);
		if (en != null) {
			busDirectionEnum = en;
			return true;
		}
		return false;
	}

    @NonNull
	public String getBusTextReceived() {
		return textReceived;
	}

    @NonNull
	public BusDirectionEnum getBusDirectionEnum() {
		return busDirectionEnum;
	}

	public enum BusDirectionEnum {

		NORTHBOUND("Northbound", "NORTH"), WESTBOUND("Westbound", "WEST"), SOUTHBOUND("Southbound", "SOUTH"), EASTBOUND("Eastbound", "EAST");

		private String text;
		private String shortUpperCase;

		BusDirectionEnum(final String text, final String shortUpperCase) {
			this.text = text;
			this.shortUpperCase = shortUpperCase;
		}

        @Nullable
		public static BusDirectionEnum fromString(final String text) {
			if (text != null) {
				for (final BusDirectionEnum b : BusDirectionEnum.values()) {
					if (text.equalsIgnoreCase(b.text)) {
						return b;
					} else if (text.equalsIgnoreCase(b.shortUpperCase)) {
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

        @NonNull
		public final String getShortUpperCase() {
			return shortUpperCase;
		}
	}
}

