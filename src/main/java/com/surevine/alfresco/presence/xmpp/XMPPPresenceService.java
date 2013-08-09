package com.surevine.alfresco.presence.xmpp;

import org.jivesoftware.smack.packet.Presence;

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
	
}
