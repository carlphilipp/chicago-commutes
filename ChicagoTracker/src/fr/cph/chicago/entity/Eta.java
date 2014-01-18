package fr.cph.chicago.entity;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import fr.cph.chicago.entity.enumeration.TrainLine;

public class Eta implements Comparable<Eta>{
	private Station station;
	private Stop stop;

	private Integer runNumber;
	private TrainLine routeName;

	//private Stop destStop;
	
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

	public Station getStation() {
		return station;
	}

	public void setStation(Station station) {
		this.station = station;
	}

	public Stop getStop() {
		return stop;
	}

	public void setStop(Stop stop) {
		this.stop = stop;
	}

	public Integer getRunNumber() {
		return runNumber;
	}

	public void setRunNumber(Integer runNumber) {
		this.runNumber = runNumber;
	}

	public TrainLine getRouteName() {
		return routeName;
	}

	public void setRouteName(TrainLine routeName) {
		this.routeName = routeName;
	}

	public Integer getTrainRouteDirectionCode() {
		return trainRouteDirectionCode;
	}

	public void setTrainRouteDirectionCode(Integer trainRouteDirectionCode) {
		this.trainRouteDirectionCode = trainRouteDirectionCode;
	}

	public Date getPredictionDate() {
		return predictionDate;
	}

	public void setPredictionDate(Date predictionDate) {
		this.predictionDate = predictionDate;
	}

	public Date getArrivalDepartureDate() {
		return arrivalDepartureDate;
	}

	public void setArrivalDepartureDate(Date arrivalDepartureDate) {
		this.arrivalDepartureDate = arrivalDepartureDate;
	}

	public Boolean getIsApp() {
		return isApp;
	}

	public void setIsApp(Boolean isApp) {
		this.isApp = isApp;
	}

	public Boolean getIsSch() {
		return isSch;
	}

	public void setIsSch(Boolean isSch) {
		this.isSch = isSch;
	}

	public Boolean getIsFlt() {
		return isFlt;
	}

	public void setIsFlt(Boolean isFlt) {
		this.isFlt = isFlt;
	}

	public Boolean getIsDly() {
		return isDly;
	}

	public void setIsDly(Boolean isDly) {
		this.isDly = isDly;
	}

	public String getFlags() {
		return flags;
	}

	public void setFlags(String flags) {
		this.flags = flags;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public Integer getHeading() {
		return heading;
	}

	public void setHeading(Integer heading) {
		this.heading = heading;
	}


	public String getTimeLeft() {
		long time = arrivalDepartureDate.getTime() - predictionDate.getTime();
		return String.format(Locale.ENGLISH, "%d min", TimeUnit.MILLISECONDS.toMinutes(time));
	}
	
	public String getTimeLeftDueDelay(){
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

	public Integer getDestSt() {
		return destSt;
	}

	public void setDestSt(Integer destSt) {
		this.destSt = destSt;
	}

	public String getDestName() {
		return destName;
	}

	public void setDestName(String destName) {
		this.destName = destName;
	}

	@Override
	public int compareTo(Eta another) {
		Long time1 = arrivalDepartureDate.getTime() - predictionDate.getTime();
		Long time2 = another.getArrivalDepartureDate().getTime() - another.getPredictionDate().getTime();
		return time1.compareTo(time2);
	}

}
