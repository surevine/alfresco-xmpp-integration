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

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.packet.Presence;

/**
 * Establishes connections to an XMPP Server  
 * 
 * @author jonnyheavey
 *
 */
public interface XMPPConnectionProvider {

	/**
	 * Establish a connection to an XMPP server
	 * 
	 * @param config containing details of the XMPP server to connect to
	 * @param user to connect as
	 * @return an active connection to the XMPP server
	 */
	public Connection getConnection(XMPPConfiguration config, XMPPUser user);
	
	public void closeConnection(XMPPConfiguration config, XMPPUser user, Presence p);
	
	public void closeConnection(XMPPConfiguration config, XMPPUser user);
	
	public void setDefaultPresenceProvider(DefaultPresenceProvider provider);
}
