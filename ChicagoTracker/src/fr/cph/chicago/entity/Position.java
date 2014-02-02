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

package fr.cph.chicago.entity;

public class Position {

	private Double latitude;
	private Double longitude;

	public Position() {
	}

	public Position(final Double latitude, final Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public final Double getLatitude() {
		return latitude;
	}

	public final void setLatitude(final Double latitude) {
		this.latitude = latitude;
	}

	public final Double getLongitude() {
		return longitude;
	}

	public final void setLongitude(final Double longitude) {
		this.longitude = longitude;
	}

	@Override
	public final String toString() {
		return "[lattitude=" + latitude + ";longitude=" + longitude + "]";
	}

}
