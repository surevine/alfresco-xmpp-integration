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
