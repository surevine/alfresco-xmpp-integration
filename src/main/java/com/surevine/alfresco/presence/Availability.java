package com.surevine.alfresco.presence;


/**
 * Enumerates the possible availability states within the application (plus an additional 'unknown' state, meaning we
 * were unable to retrieve availability information for that user)
 * 
 * @author Paul Guare
 *
 */
public enum Availability {

	/**
	 * The user is connected to the presence service and is available for chat 
	 */
	ONLINE,
	
	/**
	 * The user is connected to the presence service but has indicated they do not wish to be disturbed
	 */
	BUSY,
	
	/**
	 * The user is connected to the presence service, but might not respond to messages immediately
	 * (e.g. they might be away from their desk)
	 */
	AWAY,
	
	/**
	 * The user is not connected to the presence service
	 */
	OFFLINE,
	
	/**
	 * We do not currently have any availability information for the user (e.g. there might have been a network
	 * error requesting information from the presence service)
	 */
	UNKNOWN;
	
}
