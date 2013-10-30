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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.OfflineMessageHeader;
import org.jivesoftware.smackx.OfflineMessageManager;

/**
 * Implementation of XMPPMessageService
 * @author jheavey simonw
 *
 */
public class XMPPUnreadMessageServiceImpl extends XMPPService implements XMPPUnreadMessageService {

	private static final Log _logger = LogFactory.getLog(XMPPUnreadMessageServiceImpl.class);
	
	private XMPPPresenceService _presenceService=null;
	public void setXMPPPresenceService (XMPPPresenceService ps) {
		_presenceService=ps;
	}
	
	@Override
	public int getUnreadMessageCount(XMPPUser user) {
		
		validateUser(user);
		
		Connection connection = getConnection(user);
		
		synchronized(connection) {
			OfflineMessageManager offlineMessages = getOfflineMessageManager(connection);
			
			if (_logger.isInfoEnabled()) {
				_logger.info("Retrieving unread message count for "+user.getUsername());
			}
			
			int messageCount = 0;
			try {
				messageCount = offlineMessages.getMessageCount();
			} catch (XMPPException e) {
				_logger.error("Error retrieving unread message count for "+user.getUsername());
				resetConnection(user);
				throw new XMPPExecutionException("Error retrieving unread message count for "+user.getUsername(), e);
			}
			
			if (messageCount>0) {
				resetConnection(user);
			}
			
			return messageCount;
		}
	}
	
	@Override
	public Iterator<OfflineMessageHeader> getUnreadMessageHeaders(XMPPUser user) {
		
		validateUser(user);
		
		Connection connection = getConnection(user);
		
		synchronized (connection) {
			if (connection == null || !connection.isConnected() || !connection.isAuthenticated()) {
				_logger.error("About to attempt retrieval of unread messages with a dead connection.");
			}
			
			OfflineMessageManager offlineMessages = getOfflineMessageManager(connection);
			
			if (_logger.isInfoEnabled()) {
				_logger.info("Retrieving unread message headers for "+user.getUsername());
			}
			
			Iterator<OfflineMessageHeader> headers; 
			Collection<OfflineMessageHeader> filteredHeaders = new ArrayList<OfflineMessageHeader>();
			try {
				headers = offlineMessages.getHeaders();
				if (_logger.isDebugEnabled()) {
					_logger.debug("Found the following message headers:");
				}
				//boolean traceAllMessages=false; //See below block
				
				while (headers.hasNext()) {
					OfflineMessageHeader header = headers.next();
					if (_logger.isDebugEnabled()) {
						_logger.debug("    "+header.getJid()+"|"+header.getStamp()+"|"+header.getUser());
					}
					
					// filter out headers with invalid jid's
					if(header.getJid().contains("@")) {
						filteredHeaders.add(header);
					}
					else {
						//traceAllMessages=true; //Again, see below block
						if (_logger.isDebugEnabled()) {
							_logger.debug("Filtering out a message with JID: "+header.getJid()+" stamp "+header.getStamp()+" and user "+header.getUser());
						}
					}
				}
				/*
				 * For investigation only - do not leave in production code, even on TRACE, as it crashes the server a lot
				 */
				
				/* if (_logger.isTraceEnabled() && traceAllMessages) {
					Iterator<Message> messages = getUnreadMessages(user);
					_logger.trace("As we have filtered out a message, am now logging the bodies of offline messages");
					while (messages.hasNext()) {
						Message m = messages.next();
						_logger.trace("    "+m.toXML());
						
					}
				}*/
			} catch (Exception e) {
				_logger.error("Error retrieving unread messages for "+user.getUsername());
				resetConnection(user);
				throw new XMPPExecutionException("Error retrieving unread message headers for "+user.getUsername(), e);
			}
			
			if (filteredHeaders.size()>0) {
				resetConnection(user);
			}		
			return filteredHeaders.iterator();
		}

	}

	@Override
	public Iterator<Message> getUnreadMessages(XMPPUser user) {

		validateUser(user);
		
		Connection connection = getConnection(user);
		synchronized(connection) {
			OfflineMessageManager offlineMessages = getOfflineMessageManager(connection);
			
			if (_logger.isInfoEnabled()) {
				_logger.info("Retrieving unread messages for "+user.getUsername());
			}
			
			Iterator<Message> messages = null;
			try {
				messages = offlineMessages.getMessages();
				if (offlineMessages.getMessageCount()>0) {
					resetConnection(user);
				}
			} catch (XMPPException e) {
				_logger.error("Error retrieving unread messages for "+user.getUsername());
				resetConnection(user);
				throw new XMPPExecutionException("Error retrieving unread messages for "+user.getUsername(), e);
			}
			
			return messages;
		}
		
	}

	protected void validateUser(XMPPUser user) {
		
		if (_logger.isInfoEnabled()) {
			_logger.info("validating user: "+user);
		}
		
		if(user==null) {
			_logger.error("user cannot be null.");
			throw new XMPPExecutionException(new IllegalArgumentException("user cannot be null."));
		}
		
		if(!userExists(user.getUsername())) {
			_logger.error("User does not exist: "+user.getUsername());
			throw new XMPPExecutionException("User does not exist: "+user.getUsername());
		}
	}
	
	protected OfflineMessageManager getOfflineMessageManager(Connection connection) {
		return new OfflineMessageManager(connection);
	}
	
	/**
	 * Reset the connection for a user without changing their presence (although a new presence packet with the existing presence info will be sent)
	 * @param user
	 */
	protected void resetConnection(XMPPUser user) {
		if (_logger.isDebugEnabled()) {
			_logger.debug("Resetting connection for "+user.getUsername());
		}
		
		//First, close the existing connection, without changing the users' presence.  We need a reference to a presence service to do this - if we don't
		//have one, then just close the connection in the usual manner.  Also, don't send presence for the super-user
		try {
			if (_presenceService!=null) {
				if (user.equals(XMPPConfiguration.getConfiguration().getSuperUser())) { //Presence service, but superuser so don't reset presence
					getConnectionProvider().closeConnection(XMPPConfiguration.getConfiguration(), user);
					_presenceService.forgetSuperUserRoster();
				}
				else { //We have a presence service, no super-user so close connection and send presence
					Presence toSend = _presenceService.getPresence(user.getUsername()).convertToXMPPPresence();
					_presenceService.ignoreNextDisconnectFor(user);
					getConnectionProvider().closeConnection(XMPPConfiguration.getConfiguration(), user, toSend);
				}
			}
			else {
				if (user.equals(XMPPConfiguration.getConfiguration().getSuperUser())) { //No presence service, but a super-user
					getConnectionProvider().closeConnection(XMPPConfiguration.getConfiguration(), user);
				}
				else {//No presence service, not a super-user
					if (_logger.isInfoEnabled() && !user.equals(XMPPConfiguration.getConfiguration().getSuperUser())) {
						_logger.info("Was not provided with a reference to an XMPPPresenceService, so am closing the connection for "+user.getUsername()+" without maintaining their presence");
					}
					getConnectionProvider().closeConnection(XMPPConfiguration.getConfiguration(), user);
				}
			}
		}
		catch (Exception e) {
			_logger.warn("Could not close the connection for "+user.getUsername()+" in order to reset it.  Will establish a new connection instead", e);
		}
		
		//Next, establish a new connection as that user
		getConnection(user);
		if (_logger.isTraceEnabled()) {
			_logger.trace("Connection reset for "+user.getUsername());
		}
	}
	
}
