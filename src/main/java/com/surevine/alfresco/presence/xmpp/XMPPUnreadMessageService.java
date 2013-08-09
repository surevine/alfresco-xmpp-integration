package com.surevine.alfresco.presence.xmpp;

import java.util.Iterator;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.OfflineMessageHeader;

/**
 * Service to interact with an XMPP server to retrieve unread message information for a user.
 * Unread message service implemented by XEP-0013 (http://xmpp.org/extensions/xep-0013.html)
 * 
 * @author jonnyheavey
 *
 */
public interface XMPPUnreadMessageService {
	
	/**
	 * Retrieve number of unread messages on server for a given user
	 * 
	 * @param user to retrieve unread message count for
	 * @return number of unread messages on server
	 */
	public int getUnreadMessageCount(XMPPUser user);

	/**
	 * Retrieve unread message headers for a given user
	 * 
	 * @param user to retrieve unread message headers for
	 * @return headers of unread messages on server
	 */
	public Iterator<OfflineMessageHeader> getUnreadMessageHeaders(XMPPUser user);
	
	/**
	 * Retrieve unread messages for a given user (without consuming them)
	 * 
	 * @param user to retrieve unread messages for
	 * @return unread messages on server
	 */
	public Iterator<Message> getUnreadMessages(XMPPUser user);
	
}
