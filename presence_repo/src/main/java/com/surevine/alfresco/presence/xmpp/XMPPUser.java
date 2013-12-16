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

/**
 * Class representing a user account on an XMPP server
 * 
 * @author jonnyheavey
 *
 */
public class XMPPUser {
	
	private String username;
	private transient String password;
	
	public XMPPUser(String username, String password) {
		
		if(username==null || username.trim().equals("")) {
			throw new XMPPExecutionException(new IllegalArgumentException("null or empty username is not valid. username was: "+username));
		}
		if(password==null || password.trim().equals("")) {
			throw new XMPPExecutionException(new IllegalArgumentException("null or empty password is not valid. username was: "+password));
		}
		
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String toString() {
		return "XMPPUser[username="+username+"]";
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof XMPPUser) {
			return this.username.equals(((XMPPUser)o).username);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return username.hashCode();
	}

}
