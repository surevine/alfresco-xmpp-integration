package com.surevine.alfresco.presence.xmpp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.packet.Presence;

public class SimpleDefaultPresenceProvider implements DefaultPresenceProvider {

	private static final Log _logger = LogFactory.getLog(SimpleDefaultPresenceProvider.class);

	@Override
	public Presence getDefaultPresence(XMPPUser user) {
		if (_logger.isTraceEnabled()) {
			_logger.trace("Returning default presence");
		}
		return new Presence(Presence.Type.available, "", -2, Presence.Mode.available);
	}

}
