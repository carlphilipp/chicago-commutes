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

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import fr.cph.chicago.entity.enumeration.TrainLine;

public final class Eta implements Comparable<Eta> {
	private Station station;
	private Stop stop;

	private Integer runNumber;
	private TrainLine routeName;

	private Integer destSt;
	private String destName;

	private Integer trainRouteDirectionCode;
	private Date predictionDate;
	private Date arrivalDepartureDate;

	private Boolean isApp;
	private Boolean isSch;
	private Boolean isFlt;
	private Boolean isDly;
	private String flags;

	private Position position;

	private Integer heading;

	public final Station getStation() {
		return station;
	}

	public final void setStation(final Station station) {
		this.station = station;
	}

	public final Stop getStop() {
		return stop;
	}

	public final void setStop(final Stop stop) {
		this.stop = stop;
	}

	public final Integer getRunNumber() {
		return runNumber;
	}

	public final void setRunNumber(final Integer runNumber) {
		this.runNumber = runNumber;
	}

	public final TrainLine getRouteName() {
		return routeName;
	}

	public final void setRouteName(final TrainLine routeName) {
		this.routeName = routeName;
	}

	public final Integer getTrainRouteDirectionCode() {
		return trainRouteDirectionCode;
	}

	public final void setTrainRouteDirectionCode(final Integer trainRouteDirectionCode) {
		this.trainRouteDirectionCode = trainRouteDirectionCode;
	}

	public final Date getPredictionDate() {
		return predictionDate;
	}

	public final void setPredictionDate(final Date predictionDate) {
		this.predictionDate = predictionDate;
	}

	public final Date getArrivalDepartureDate() {
		return arrivalDepartureDate;
	}

	public final void setArrivalDepartureDate(final Date arrivalDepartureDate) {
		this.arrivalDepartureDate = arrivalDepartureDate;
	}

	public final Boolean getIsApp() {
		return isApp;
	}

	public final void setIsApp(final Boolean isApp) {
		this.isApp = isApp;
	}

	public final Boolean getIsSch() {
		return isSch;
	}

	public final void setIsSch(final Boolean isSch) {
		this.isSch = isSch;
	}

	public final Boolean getIsFlt() {
		return isFlt;
	}

	public final void setIsFlt(final Boolean isFlt) {
		this.isFlt = isFlt;
	}

	public final Boolean getIsDly() {
		return isDly;
	}

	public final void setIsDly(final Boolean isDly) {
		this.isDly = isDly;
	}

	public final String getFlags() {
		return flags;
	}

	public final void setFlags(final String flags) {
		this.flags = flags;
	}

	public final Position getPosition() {
		return position;
	}

	public final void setPosition(final Position position) {
		this.position = position;
	}

	public final Integer getHeading() {
		return heading;
	}

	public final void setHeading(final Integer heading) {
		this.heading = heading;
	}

	public final String getTimeLeft() {
		long time = arrivalDepartureDate.getTime() - predictionDate.getTime();
		return String.format(Locale.ENGLISH, "%d min", TimeUnit.MILLISECONDS.toMinutes(time));
	}

	public final String getTimeLeftDueDelay() {
		String result;
		if (getIsDly()) {
			result = "Delay";
		} else {
			if (getIsApp()) {
				result = "Due";
			} else {
				result = getTimeLeft();
			}
		}
		return result;
	}

	public final Integer getDestSt() {
		return destSt;
	}

	public final void setDestSt(final Integer destSt) {
		this.destSt = destSt;
	}

	public final String getDestName() {
		return destName;
	}

	public final void setDestName(String destName) {
		this.destName = destName;
	}

	@Override
	public final int compareTo(final Eta another) {
		Long time1 = arrivalDepartureDate.getTime() - predictionDate.getTime();
		Long time2 = another.getArrivalDepartureDate().getTime() - another.getPredictionDate().getTime();
		return time1.compareTo(time2);
	}
}
