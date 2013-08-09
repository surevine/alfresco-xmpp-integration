package com.surevine.alfresco.presence;

import org.alfresco.repo.processor.BaseProcessorExtension;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Makes the specified presence service available to JavaScript based web scripts
 * 
 * @author Paul Guare
 * 
 */
public class PresenceScript extends BaseProcessorExtension {
	
	private static final Log _logger = LogFactory.getLog(PresenceScript.class);
	
	/** The presence service this object exposes to JavaScript based web scripts **/
	private PresenceService presenceService;

	/**
	 * @return The presence service this object exposes to JavaScript based web scripts
	 */
	public PresenceService getPresenceService() {
		return presenceService;
	}

	/**
	 * @param presenceService The presence service this object should expose to JavaScript based web scripts
	 */
	public void setPresenceService(PresenceService presenceService) {
		this.presenceService = presenceService;
	}
	
	/**
	 * Retrieves presence information for the specified user using the configured presence service.
	 * 
	 * Any errors returned by the presence service are suppressed, and will cause this method to return
	 * a response indicating that we do not have presence information for that user.
	 * 
	 * @param userName ID of the user whose presence information we are requesting
	 * @return Presence information for the specified user
	 */
	public Presence getUserPresence(final String userName) {
			return presenceService.getUserPresence(userName, false);
	}

}
