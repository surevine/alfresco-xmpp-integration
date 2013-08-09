package com.surevine.alfresco.presence;

/**
 * Thrown if an error occurs retrieving presence information for a given user 
 * 
 * @author Paul Guare
 *
 */
public class PresenceException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public PresenceException() { }
	
	public PresenceException(Throwable cause) {
		super(cause);
	}
	
	public PresenceException(String message) {
		super(message);
	}
	
	public PresenceException(String message, Throwable cause) {
		super(message, cause);
	}

}
