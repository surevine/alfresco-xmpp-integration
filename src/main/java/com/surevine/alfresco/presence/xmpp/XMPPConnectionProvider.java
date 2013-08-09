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
