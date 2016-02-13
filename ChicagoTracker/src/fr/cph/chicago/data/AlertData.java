/**
 * Copyright 2016 Carl-Philipp Harmant
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.entity.Alert;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.xml.Xml;

/**
 * Class that handle alert data. Singleton
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class AlertData {
	/** The data **/
	private static AlertData mAlertData;
	/** The list of alerts **/
	private List<Alert> mAlerts;

	/**
	 * Private constructor
	 */
	private AlertData() {
		mAlerts = new ArrayList<Alert>();
	}

	/**
	 * Singleton access to this class
	 * 
	 * @return the data
	 */
	public static AlertData getInstance() {
		if (mAlertData == null) {
			mAlertData = new AlertData();
		}
		return mAlertData;
	}

	/**
	 * Connect to CTA API to get the alerts messages
	 * 
	 * @return a list of alert
	 * @throws ParserException
	 *             a parse exception
	 * @throws ConnectException
	 *             a connect exception
	 */
	public final List<Alert> loadGeneralAlerts() throws ParserException, ConnectException {
		if (mAlerts.size() == 0) {
			MultiMap<String, String> params = new MultiValueMap<String, String>();
			CtaConnect connect = CtaConnect.getInstance();
			Xml xml = new Xml();
			String xmlResult = connect.connect(CtaRequestType.ALERTS_GENERAL, params);
			mAlerts = xml.parseAlertGeneral(xmlResult);
		}
		return mAlerts;
	}

	/**
	 * Get list of alert
	 * 
	 * @return a list of alert
	 */
	public final List<Alert> getAlerts() {
		return this.mAlerts;
	}

}
