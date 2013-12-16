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
package com.surevine.alfresco.presence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.OfflineMessageHeader;

import com.surevine.alfresco.presence.xmpp.SimplePresence;
import com.surevine.alfresco.presence.xmpp.SimplePresence.Availability;
import com.surevine.alfresco.presence.xmpp.XMPPExecutionException;
import com.surevine.alfresco.presence.xmpp.XMPPPresenceService;
import com.surevine.alfresco.presence.xmpp.XMPPUnreadMessageService;
import com.surevine.alfresco.presence.xmpp.XMPPUser;

/**
 * Exposes XMPPPresenceService and XMPPUnreadMessageService to the javascript API.
 * 
 * Authenticates using the super-user-credential, and therefore depending upon the
 * relevant authentication module being installed into the underlying openfire server 
 * 
 * @author simonw
 *
 */
public class XMPPScriptService extends BaseScopableProcessorExtension {
	
	private static final Log _logger = LogFactory.getLog(XMPPScriptService.class);

	//Injected
	private XMPPPresenceService _presenceService;
	public void setXMPPPresenceService(XMPPPresenceService service) {
		_presenceService=service;
	}
	
	//Injected
	private XMPPUnreadMessageService _unreadMessageService;
	public void setXMPPUnreadMessageService(XMPPUnreadMessageService service) {
		_unreadMessageService=service;
	}
	
	/**
	 * The superuser credentials - defaults to a value that will work on a specific test instance,
	 * but it should be best-practice to always set this via Spring
	 */
	private transient String _superCredentials="T3G0gD1dJxp1sfko";
	public void setSuperuserCredentials(String credentials) {
		_superCredentials=credentials;
	}
	
	public ScriptPresence getPresence(String username) {
		if (_logger.isTraceEnabled()) {
			_logger.trace("Getting presence for "+username);
		}
		SimplePresence p = _presenceService.getPresence(username);
		if (_logger.isDebugEnabled()) {
			_logger.debug("Presence for "+username+": "+p);
		}
		
		//Calculate Mode, Status & Source
		String scriptAvailability="unknown";
		String scriptStatus;
		String scriptSource;
		if (p!=null) {
			switch (p.getAvailability()) {
			case AVAILABLE:
				scriptAvailability="available";
				break;
			case BUSY:
				scriptAvailability="busy";
				break;
			case OFFLINE:
				scriptAvailability="offline";
			}
			
			//OK, we've got or have inferred the presence mode at this point, now we need the status
			scriptStatus = p.getMessage();
			
			//Now we've got the status, let's get the source;
			scriptSource=p.getSource();
		}
		else {
			if(_logger.isDebugEnabled()) {
				_logger.debug("Could not find a presence for "+username+" so assuming offline");
			}
			scriptAvailability="offline";
			scriptStatus="";
			scriptSource="";
		}
		
		return new ScriptPresence(scriptStatus, scriptAvailability, scriptSource, username);
	}
	
	public boolean setPresence(String username, String mode) {
		return setPresence(username,  mode, "");
	}
	
	public boolean setPresence(String username, String availabilityStr, String message) {
		XMPPUser user = new XMPPUser(username, _superCredentials);
		
		Availability availability;
		if (availabilityStr.trim().equalsIgnoreCase("available")) {
			availability=Availability.AVAILABLE;
		}
		else if (availabilityStr.trim().equalsIgnoreCase("busy")) {
			availability=Availability.BUSY;
		}
		else if (availabilityStr.trim().equalsIgnoreCase("offline")) {
			availability=Availability.OFFLINE;
		}
		else {
			throw new XMPPExecutionException(new com.sun.star.lang.IllegalArgumentException("The availability string '"+availabilityStr+"' is not valid.  Only [available|busy|offline] are allowed"));
		}
		
		SimplePresence newPresence = new SimplePresence(availability, user, "space", message);
		
		try {
			_presenceService.setPresence(newPresence);
			return true; 
		}
		catch (Exception e) {
			_logger.error("Could not set presence: "+newPresence, e);
			return false;
		}
	}
	
	public int getUnreadMessageCount(String username) {
		return _unreadMessageService.getUnreadMessageCount(new XMPPUser(username, _superCredentials));
	}
	
	public Collection<OfflineMessageHeader> getUnreadMessageHeaders(String username) {
		Iterator<OfflineMessageHeader> headers = _unreadMessageService.getUnreadMessageHeaders(new XMPPUser(username, _superCredentials));
		Collection<OfflineMessageHeader> plainHeaders = new ArrayList<OfflineMessageHeader>();
		while (headers.hasNext()) {
			plainHeaders.add(headers.next());
		}
		if (_logger.isDebugEnabled()) {
			_logger.debug("Returning "+plainHeaders.size()+" unread message headers for "+username);
		}
		return plainHeaders;
	}
	
	public Collection<Message> getUnreadMessages(String username) {
		Iterator<Message> messages = _unreadMessageService.getUnreadMessages(new XMPPUser(username, _superCredentials));
		Collection<Message> plainMessages = new ArrayList<Message>();
		while (messages.hasNext()) {
			plainMessages.add(messages.next());
		}
		return plainMessages;
	}

}
