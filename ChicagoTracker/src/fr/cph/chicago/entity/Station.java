package fr.cph.chicago.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import fr.cph.chicago.entity.enumeration.TrainLine;

public class Station implements Comparable<Station> {
	private Integer id;
	private String name;
	private List<Stop> stops;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Stop> getStops() {
		return stops;
	}

	public void setStops(List<Stop> stops) {
		this.stops = stops;
	}

	@Override
	public String toString() {
		StringBuilder stb = new StringBuilder();
		stb.append("[Id=" + id);
		stb.append(";name=" + name);
		if (stops != null) {
			stb.append(";stops=" + stops);
		}
		if (getLines() != null) {
			stb.append(";lines=" + getLines());
		}
		stb.append("]");
		return stb.toString();
	}

	public final Set<TrainLine> getLines() {
		if (stops != null) {
			Set<TrainLine> lines = new TreeSet<TrainLine>();
			for (Stop stop : stops) {
				for (TrainLine tl : stop.getLines()) {
					lines.add(tl);
				}
			}
			// PURPLE_EXPRESS MOD
//			if (lines.contains(TrainLine.PURPLE) && lines.contains(TrainLine.PURPLE_EXPRESS)) {
//				lines.remove(TrainLine.PURPLE_EXPRESS);
//			}
			return lines;
		} else {
			return null;
		}
	}

	public final Map<TrainLine, List<Stop>> getStopByLines() {
		Map<TrainLine, List<Stop>> map = new TreeMap<TrainLine, List<Stop>>();
		List<Stop> stops = getStops();
		for (Stop stop : stops) {
			List<TrainLine> lines = stop.getLines();
			for (TrainLine tl : lines) {
				List<Stop> stopss;
				if (map.containsKey(tl)) {
					stopss = map.get(tl);
					stopss.add(stop);
				} else {
					stopss = new ArrayList<Stop>();
					stopss.add(stop);
					map.put(tl, stopss);
				}
				
			}
		}
		//Collections.sort(map);
		return map;
	}

	@Override
	public int compareTo(Station another) {
		return this.getName().compareTo(another.getName());
	}
}
