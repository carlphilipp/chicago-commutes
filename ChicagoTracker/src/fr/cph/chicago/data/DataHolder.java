/**
 * Copyright 2016 Carl-Philipp Harmant
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

package fr.cph.chicago.data;

/**
 * Class that hold bus train and alerts data. Singleton
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class DataHolder {

	/** Singleton **/
	private static DataHolder mDataHolder;
	/** Train data **/
	private TrainData mTrainData;
	/** Bus data **/
	private BusData mBusData;
	/** Alert data **/
	private AlertData mAlertData;

	/**
	 * Private constructor
	 */
	private DataHolder() {
	}

	/**
	 * Get instance of the class. Singleton
	 *
	 * @return
	 */
	public static final DataHolder getInstance() {
		if (mDataHolder == null) {
			mDataHolder = new DataHolder();
		}
		return mDataHolder;
	}

	/**
	 * Get Train data
	 *
	 * @return train data
	 */
	public final TrainData getTrainData() {
		return mTrainData;
	}

	/**
	 * Set train data
	 *
	 * @param data
	 *            train data
	 */
	public final void setTrainData(final TrainData data) {
		this.mTrainData = data;
	}

	/**
	 * Get bus data
	 *
	 * @return bus data
	 */
	public final BusData getBusData() {
		return mBusData;
	}

	/**
	 * Set bus data
	 *
	 * @param busData
	 *            bus data
	 */
	public final void setBusData(final BusData busData) {
		this.mBusData = busData;
	}

	/**
	 * Get alert data
	 *
	 * @return alert data
	 */
	public final AlertData getAlertData() {
		return mAlertData;
	}

	/**
	 * Set alert data
	 *
	 * @param alertData
	 *            alert data
	 */
	public final void setAlertData(final AlertData alertData) {
		this.mAlertData = alertData;
	}
}
