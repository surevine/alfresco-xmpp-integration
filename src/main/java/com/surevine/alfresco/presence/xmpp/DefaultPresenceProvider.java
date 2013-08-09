package com.surevine.alfresco.presence.xmpp;

import org.jivesoftware.smack.packet.Presence;

public interface DefaultPresenceProvider {

	public Presence getDefaultPresence(XMPPUser user);
}
