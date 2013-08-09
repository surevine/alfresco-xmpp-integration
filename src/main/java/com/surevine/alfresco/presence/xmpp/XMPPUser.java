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
