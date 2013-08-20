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
