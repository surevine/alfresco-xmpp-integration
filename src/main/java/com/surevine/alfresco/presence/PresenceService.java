package com.surevine.alfresco.presence;

/**
 * Defines operations for retrieving presence information (availability, status, etc.) for a specified user
 * 
 * @author Paul Guare
 *
 */
public interface PresenceService {

	/**
	 * Returns presence information for the specified user
	 * 
	 * @param userName ID of the user whose presence information we are requesting
	 * @return Presence information for the specified user
	 */
	public Presence getUserPresence(String userName);
	
	/**
	 * Returns presence information for the specified user
	 * 
	 * @param userName ID of the user whose presence information we are requesting
	 * @param A flag indicating if the system should wait an amount of time for the offline user
	 * to become online, if the given username is offline.  The wait should be at least 20ms, but may
	 * be higher at the discretion of the implementing class.  The implementing class is not bound
	 * to honour this wait request
	 * @return Presence information for the specified user
	 */
	public Presence getUserPresence(String userName, boolean waitIfOffline);
	
}
