package fr.cph.chicago.exception;

public class TrackerException extends Exception {

	/** **/
	private static final long serialVersionUID = 1L;

	public TrackerException(String message, Exception e) {
		super(message, e);
	}

}
