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
