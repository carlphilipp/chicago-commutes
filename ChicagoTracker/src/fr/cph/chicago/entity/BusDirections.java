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
import java.util.List;

import fr.cph.chicago.entity.enumeration.BusDirection;

public final class BusDirections {
	private String id;
	private List<BusDirection> lBusDirection;
	
	public BusDirections(){
		lBusDirection = new ArrayList<BusDirection>();
	}

	public final String getId() {
		return id;
	}

	public final void setId(final String id) {
		this.id = id;
	}

	public final List<BusDirection> getlBusDirection() {
		return lBusDirection;
	}

	public final void setlBusDirection(final List<BusDirection> lBusDirection) {
		this.lBusDirection = lBusDirection;
	}
	
	public final void addBusDirection(final BusDirection dir){
		lBusDirection.add(dir);
	}

}
