package fr.cph.chicago.entity;

import java.util.List;

public class Train {

	private int routeNumber;
	private int destStation;
	private String destName;
	private int routeDirection;
	private boolean app;
	private boolean dly;
	private Position position;
	private int heading;

	public int getRouteNumber() {
		return routeNumber;
	}

	public final void setRouteNumber(final int routeNumber) {
		this.routeNumber = routeNumber;
	}

	public final int getDestStation() {
		return destStation;
	}

	public final void setDestStation(final int destStation) {
		this.destStation = destStation;
	}

	public final String getDestName() {
		return destName;
	}

	public final void setDestName(final String destName) {
		this.destName = destName;
	}

	public final int getRouteDirection() {
		return routeDirection;
	}

	public final void setRouteDirection(final int routeDirection) {
		this.routeDirection = routeDirection;
	}

	public final boolean isApp() {
		return app;
	}

	public final void setApp(final boolean app) {
		this.app = app;
	}

	public final boolean isDly() {
		return dly;
	}

	public final void setDly(final boolean dly) {
		this.dly = dly;
	}

	public final Position getPosition() {
		return position;
	}

	public final void setPosition(final Position position) {
		this.position = position;
	}

	public final int getHeading() {
		return heading;
	}

	public final void setHeading(final int heading) {
		this.heading = heading;
	}
	
	public static Position getBestPosition(final List<Train> trains) {
		Position position = new Position();
		double maxLatitude = 0.0;
		double minLatitude = 0.0;
		double maxLongitude = 0.0;
		double minLongitude = 0.0;
		int i = 0;
		for (Train train : trains) {
			Position temp = train.getPosition();
			if (i == 0) {
				maxLatitude = temp.getLatitude();
				minLatitude = temp.getLatitude();
				maxLongitude = temp.getLongitude();
				minLongitude = temp.getLongitude();
			} else {
				if (temp.getLatitude() > maxLatitude) {
					maxLatitude = temp.getLatitude();
				}
				if (temp.getLatitude() < minLatitude) {
					minLatitude = temp.getLatitude();
				}
				if (temp.getLongitude() > maxLongitude) {
					maxLongitude = temp.getLongitude();
				}
				if (temp.getLongitude() < minLongitude) {
					minLongitude = temp.getLongitude();
				}
			}
			i++;
		}
		position.setLatitude((maxLatitude + minLatitude) / 2);
		position.setLongitude((maxLongitude + minLongitude) / 2);
		return position;
	}
}
