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
