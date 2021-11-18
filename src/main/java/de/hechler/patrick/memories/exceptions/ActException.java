package de.hechler.patrick.memories.exceptions;


public class ActException extends RuntimeException {
	
	/** UID */
	private static final long serialVersionUID = -2087617829040400568L;
	
	public ActException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ActException(String message) {
		super(message);
	}
	
	public ActException(Throwable cause) {
		super(cause);
	}
	
}
