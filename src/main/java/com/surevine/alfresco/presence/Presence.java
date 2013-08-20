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

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;

/**
 * Encapsulates all presence information for a single user (i.e. their availability and status)
 * 
 * Extended to include the host and username that the presence information relates to.
 *
 * @author Paul Guare
 * @author sjwelch
 * @author Simon White
 */
public class Presence implements Serializable {

	private static final long serialVersionUID = 1L;

	/** The user's availability, e.g. online, offline, etc. **/
	private Availability availability;
	
	/** A user specified, human readable description of the user's status (e.g. "gone to the shops") */
	private String status;
	
	/** The host or origin of this presence information. Can be used later to contact a user */
	private String host;
	
	/** The user name of that this presence status relates to  **/
	private String userName;
	
	/** Is the XMPP service enabled on this server **/
	private boolean serviceEnabled = false;
	
	private String fullUserName=null;
	
	
	/**
	 * Creates a new Presence object with the specified availability and human readable status together 
	 * with details on the XMPP account that this presence relates to.
	 * @param availability The user's availability, e.g. online, offline, etc.
	 * @param status A user specified, human readable description of the user's status (e.g. gone to the shops)
	 * @param userName The user account name
	 * @param host The XMPP host (ipAddress or hostname)
	 * @param isServiceEnabled Whether the remote XMPP service is enabled
	 */
	public Presence(Availability availability, String status, String userName,
			String host, boolean isServiceEnabled) {
		this.availability = availability;
		this.status = status;
		this.userName = userName;
		this.host = host;
		this.serviceEnabled = isServiceEnabled;
	}

	/**
	 * @return The user's availability, e.g. online, offline, etc.
	 */
	public Availability getAvailability() {
		return availability;
	}
	
	
	/**
	 * @return A user specified, human readable description of the user's status (e.g. gone to the shops)
	 */
	public String getStatus() {
		return status;
	}
	
	
	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	
	/**
	 * The XMPP Jabber identifier for the related account that this presence information relates to.
	 * @return the computed Jabber Id for the presence.
	 */
	public String getJid()
	{
		return getUserName() + "@" + getHost();
	}

	/**
	 * @return the serviceEnabled
	 */
	public boolean isServiceEnabled() {
		return serviceEnabled;
	}
	
	public String toJSONString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("{\"name\":\"");
		sb.append(userName);
		sb.append("\",\"status\":\"");
		sb.append(status);
		sb.append("\",\"host\":\"");
		sb.append(host);
		sb.append("\",\"availability\":\"");
		sb.append(availability);
		sb.append("\",\"enabled\":\"");
		sb.append(serviceEnabled);
		sb.append("\"");
		
		if (fullUserName!=null) {
			sb.append(",\"fullName\":\"").append(fullUserName).append("\"");
		}
		
		sb.append("}");
		return sb.toString();
	}
	
	public String toString() {
		return toJSONString();
	}
	
	public String getFullUserName(PersonService personService, NodeService nodeService) {
		if (fullUserName==null) {
			NodeRef personNode = personService.getPerson(userName);
			StringBuilder fullNameB = new StringBuilder(50);
			fullNameB.append(nodeService.getProperty(personNode, ContentModel.PROP_FIRSTNAME));
			fullNameB.append(" ");
			fullNameB.append(nodeService.getProperty(personNode, ContentModel.PROP_LASTNAME));
			fullUserName = fullNameB.toString();
		}
		return fullUserName;
		
	}

	public boolean equals(Object o) {
		if (!(o instanceof Presence)) {
			return false;
		}
		else return ((Presence)o).getUserName().equals(getUserName());
	}
	
	public int hashCode() {
		return userName.hashCode();
	}
	
	
}
