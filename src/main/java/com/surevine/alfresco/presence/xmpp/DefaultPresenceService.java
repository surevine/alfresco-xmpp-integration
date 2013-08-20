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

import org.apache.log4j.Logger;

import com.surevine.alfresco.presence.Presence;
import com.surevine.alfresco.presence.PresenceService;
import com.surevine.alfresco.presence.xmpp.SimplePresence.Availability;

public class DefaultPresenceService implements PresenceService {
	
	private static final Logger LOG = Logger.getLogger(DefaultPresenceService.class.getName());

	private boolean remoteServiceEnabled = true;
	public void setRemoteServiceEnabled(final boolean enabled) {
		this.remoteServiceEnabled = enabled;
	}
	
	private String host;
	public void setHost(final String host) {
		this.host = host;
	}
	
	private XMPPPresenceService xmppPresenceService;
	public void setXMPPPresenceService(final XMPPPresenceService xmppPresenceService) {
		this.xmppPresenceService = xmppPresenceService;
	}

	@Override
	public Presence getUserPresence(final String userName, boolean waitIfOffline) {
		try {
			final SimplePresence simplePresence = xmppPresenceService.getPresence(userName, waitIfOffline);
			
			return getPresenceFromSimplePresence(simplePresence, userName);
		} catch (final Exception e) {
			LOG.warn("Error finding presence information for user " +userName, e);
			return new Presence(com.surevine.alfresco.presence.Availability.UNKNOWN,
					"", userName, host, remoteServiceEnabled);
		}
	}
	
	public Presence getUserPresence(final String user) {
		return getUserPresence(user, true);
	}
		
	protected Presence getPresenceFromSimplePresence(final SimplePresence simplePresence, final String userName) {
		final Availability simpleAvailability = simplePresence.getAvailability();
		
		final com.surevine.alfresco.presence.Availability availability;
		
		switch (simpleAvailability) {
		case AVAILABLE:
			availability = com.surevine.alfresco.presence.Availability.ONLINE;
			break;
		case BUSY:
			availability = com.surevine.alfresco.presence.Availability.BUSY;
			break;
		case OFFLINE:
			availability = com.surevine.alfresco.presence.Availability.OFFLINE;
			break;
		default:
			availability = com.surevine.alfresco.presence.Availability.UNKNOWN;
			break;
		}
		
		return new Presence(availability, simplePresence.getMessage(), userName,
				host, remoteServiceEnabled);
	}
}
