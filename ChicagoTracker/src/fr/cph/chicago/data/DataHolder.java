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

package fr.cph.chicago.data;

/**
 * 
 * @author carl
 * 
 */
public class DataHolder {

	/** **/
	private static DataHolder dataHolder;
	/** **/
	private TrainData trainData;
	/** **/
	private BusData busData;
	/** **/
	private AlertData alertData;

	/**
	 * 
	 */
	private DataHolder() {
	}

	/**
	 * 
	 * @return
	 */
	public static final DataHolder getInstance() {
		if (dataHolder == null) {
			dataHolder = new DataHolder();
		}
		return dataHolder;
	}

	/**
	 * 
	 * @return
	 */
	public final TrainData getTrainData() {
		return trainData;
	}

	/**
	 * 
	 * @param data
	 */
	public final void setTrainData(final TrainData data) {
		this.trainData = data;
	}

	/**
	 * 
	 * @return
	 */
	public final BusData getBusData() {
		return busData;
	}

	/**
	 * 
	 * @param busData
	 */
	public final void setBusData(final BusData busData) {
		this.busData = busData;
	}

	/**
	 * 
	 * @return
	 */
	public final AlertData getAlertData() {
		return alertData;
	}

	/**
	 * 
	 * @param alertData
	 */
	public final void setAlertData(final AlertData alertData) {
		this.alertData = alertData;
	}
}
