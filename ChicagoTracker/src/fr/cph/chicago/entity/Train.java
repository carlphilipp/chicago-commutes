package fr.cph.chicago.entity;

import java.util.Date;

public class Train {
	private Integer runNumber;
	
	private Station destStation;
	private Station nextStation;
	private Stop nextStop;
	
	private Integer trainRouteDirectionCode;
	
	private Date predictionDate;
	private Date arrivalDepartureDate;
	
	private Boolean isApp;
	private Boolean isDly;
	private String flags;
	
	private Position position;
	
	private Integer heading;
}
