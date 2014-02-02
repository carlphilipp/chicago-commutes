package fr.cph.chicago.exception;

public class ParserException extends TrackerException {

	/* */
	private static final long serialVersionUID = 1L;

	public ParserException(String message, Exception e) {
		super(message, e);
	}

}
