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
 * 
 * @author carl
 * 
 */
public class AlertData {
	/** **/
	private static AlertData alertData;
	/** **/
	private List<Alert> alerts;

	/**
	 * 
	 */
	private AlertData() {
		alerts = new ArrayList<Alert>();
	}

	/**
	 * 
	 * @return
	 */
	public static AlertData getInstance() {
		if (alertData == null) {
			alertData = new AlertData();
		}
		return alertData;
	}

	/**
	 * 
	 * @return
	 * @throws ParserException
	 * @throws ConnectException
	 */
	public final List<Alert> loadGeneralAlerts() throws ParserException, ConnectException {
		if (alerts.size() == 0) {
			MultiMap<String, String> params = new MultiValueMap<String, String>();
			CtaConnect connect = CtaConnect.getInstance();
			Xml xml = new Xml();
			String xmlResult = connect.connect(CtaRequestType.ALERTS_GENERAL, params);
			alerts = xml.parseAlertGeneral(xmlResult);
		}
		return alerts;
	}

	/**
	 * 
	 * @return
	 */
	public final List<Alert> getAlerts() {
		return this.alerts;
	}

}
