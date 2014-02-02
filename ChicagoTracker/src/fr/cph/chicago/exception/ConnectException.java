package fr.cph.chicago.exception;

public class ConnectException extends TrackerException{

	private static final long serialVersionUID = 1L;
	
	public ConnectException(String message, Exception e){
		super(message, e);
	}

}
