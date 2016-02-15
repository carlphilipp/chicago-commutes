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

import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.connection.CtaRequestType;
import fr.cph.chicago.entity.Alert;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.xml.Xml;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that handle alert data. Singleton
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class AlertData {
	/**
	 * The data
	 **/
	private static AlertData alertData;
	/**
	 * The list of alerts
	 **/
	private List<Alert> alerts;

	/**
	 * Private constructor
	 */
	private AlertData() {
		alerts = new ArrayList<>();
	}

	/**
	 * Singleton access to this class
	 *
	 * @return the data
	 */
	public static AlertData getInstance() {
		if (alertData == null) {
			alertData = new AlertData();
		}
		return alertData;
	}

	/**
	 * Connect to CTA API to get the alerts messages
	 *
	 * @return a list of alert
	 * @throws ParserException  a parse exception
	 * @throws ConnectException a connect exception
	 */
	public final List<Alert> loadGeneralAlerts() throws ParserException, ConnectException {
		if (alerts.size() == 0) {
			MultiValuedMap<String, String> params = new ArrayListValuedHashMap<>();
			CtaConnect connect = CtaConnect.getInstance();
			Xml xml = new Xml();
			String xmlResult = connect.connect(CtaRequestType.ALERTS_GENERAL, params);
			alerts = xml.parseAlertGeneral(xmlResult);
		}
		return alerts;
	}

	/**
	 * Get list of alert
	 *
	 * @return a list of alert
	 */
	public final List<Alert> getAlerts() {
		return this.alerts;
	}
}
