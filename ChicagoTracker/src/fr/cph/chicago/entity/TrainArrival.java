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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.cph.chicago.entity.enumeration.TrainLine;

public class TrainArrival {
	private Date timeStamp;
	private Integer errorCode;
	private String errorMessage;
	private List<Eta> etas;

	public final Date getTimeStamp() {
		return timeStamp;
	}

	public final void setTimeStamp(final Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	public final Integer getErrorCode() {
		return errorCode;
	}

	public final void setErrorCode(final Integer errorCode) {
		this.errorCode = errorCode;
	}

	public final String getErrorMessage() {
		return errorMessage;
	}

	public final void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public final List<Eta> getEtas() {
		return etas;
	}

	public final void setEtas(final List<Eta> etas) {
		this.etas = etas;
	}

	public final List<Eta> getEtas(final TrainLine line) {
		List<Eta> etas = new ArrayList<Eta>();
		for (Eta eta : getEtas()) {
			if (eta.getRouteName() == line) {
				etas.add(eta);
			}
		}
		return etas;
	}
}
