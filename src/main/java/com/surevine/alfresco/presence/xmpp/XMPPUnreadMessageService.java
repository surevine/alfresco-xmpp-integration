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
