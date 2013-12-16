/*
 * Copyright (C) 2008-2010 Surevine Limited.
 *   
 * Although intended for deployment and use alongside Alfresco this module should
 * be considered 'Not a Contribution' as defined in Alfresco'sstandard contribution agreement, see
 * http://www.alfresco.org/resource/AlfrescoContributionAgreementv2.pdf
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
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
