package fr.cph.chicago.entity;

import java.util.ArrayList;
import java.util.List;

import fr.cph.chicago.entity.enumeration.BusDirection;

public class BusDirections {
	private String id;
	private List<BusDirection> lBusDirection;
	
	public BusDirections(){
		lBusDirection = new ArrayList<BusDirection>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<BusDirection> getlBusDirection() {
		return lBusDirection;
	}

	public void setlBusDirection(List<BusDirection> lBusDirection) {
		this.lBusDirection = lBusDirection;
	}
	
	public void addBusDirection(BusDirection dir){
		lBusDirection.add(dir);
	}

}
