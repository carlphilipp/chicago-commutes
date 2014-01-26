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

import java.util.List;

import fr.cph.chicago.entity.enumeration.TrainDirection;
import fr.cph.chicago.entity.enumeration.TrainLine;

public class Stop implements Comparable<Stop> {
	private Integer id;
	private String description;
	private TrainDirection direction;
	private Position position;

	private Boolean ada;
	private List<TrainLine> lines;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TrainDirection getDirection() {
		return direction;
	}

	public void setDirection(TrainDirection direction) {
		this.direction = direction;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public List<TrainLine> getLines() {
		return lines;
	}

	public void setLines(List<TrainLine> lines) {
		this.lines = lines;
	}

	public Boolean getAda() {
		return ada;
	}

	public void setAda(Boolean ada) {
		this.ada = ada;
	}

	@Override
	public String toString() {
		StringBuilder stb = new StringBuilder();
		stb.append("[Id=" + id);
		if (description != null) {
			stb.append(";description=" + description);
		}
		if (direction != null) {
			stb.append(";direction=" + direction);
		}
		if (position != null) {
			stb.append(";position=" + position);
		}
		if (ada != null) {
			stb.append(";ada=" + ada);
		}
		if (lines != null) {
			stb.append(";lines=" + lines);
		}
		stb.append("]");
		return stb.toString();
	}

	@Override
	public int compareTo(Stop anotherStop) {
		return this.direction.compareTo(anotherStop.getDirection());
	}
}
