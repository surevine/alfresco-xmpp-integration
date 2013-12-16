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
package com.surevine.alfresco.presence.xmpp;

/**
 * Service to interact with an XMPP server to get/set presence information for a user.
 * 
 * @author jonnyheavey
 *
 */
public interface XMPPPresenceService {

	/**
	 * Retrieve the XMPP presence of a user
	 * 
	 * @param username user to obtain presence of
	 * @return presence of the specified user
	 */
	public SimplePresence getPresence(String username);
	public SimplePresence getPresence(String username, boolean waitIfOffline);
	
	/**
	 * Set the XMPP presence for a user
	 * 
	 * @param user
	 * @param mode
	 * @param message
	 */
	public void setPresence(SimplePresence simplePresence);
	
	public void forgetSuperUserRoster();
	
	public void ignoreNextDisconnectFor(XMPPUser user);
	
}
