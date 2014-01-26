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

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	public Integer getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public List<Eta> getEtas() {
		return etas;
	}

	public void setEtas(List<Eta> etas) {
		this.etas = etas;
	}

	public List<Eta> getEtas(TrainLine line) {
		List<Eta> etas = new ArrayList<Eta>();
		for (Eta eta : getEtas()) {
			if (eta.getRouteName() == line) {
				etas.add(eta);
			}
		}
		return etas;
	}
}
