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
