/**
 * Copyright 2017 Carl-Philipp Harmant
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.connection;

/**
 * Enum that store the different kind of query we can do
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public enum CtaRequestType {
	TRAIN_ARRIVALS,
	TRAIN_FOLLOW,
	TRAIN_LOCATION,
	BUS_ROUTES,
	BUS_DIRECTION,
	BUS_STOP_LIST,
	BUS_ARRIVALS,
	BUS_PATTERN,
	BUS_VEHICLES,
	ALERTS_GENERAL,
	ALERTS_ROUTES
}
