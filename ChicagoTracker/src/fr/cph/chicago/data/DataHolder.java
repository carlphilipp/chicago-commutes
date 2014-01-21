package fr.cph.chicago.data;

import java.util.List;

import fr.cph.chicago.entity.BusStop;

public class DataHolder {

	private static DataHolder dataHolder;
	
	private TrainData trainData;
	private BusData busData;

	private DataHolder() {
	}

	public static DataHolder getInstance() {
		if (dataHolder == null) {
			dataHolder = new DataHolder();
		} 
		return dataHolder;
	}
	
	public TrainData getTrainData(){
		return trainData;
	}
	
	public void setTrainData(TrainData data){
		this.trainData = data;
	}

	public BusData getBusData() {
		return busData;
	}

	public void setBusData(BusData busData) {
		this.busData = busData;
	}
}
