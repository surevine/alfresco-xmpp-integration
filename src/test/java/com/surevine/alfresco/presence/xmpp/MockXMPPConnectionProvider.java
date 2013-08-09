package com.surevine.alfresco.presence.xmpp;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MockRoster;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;


/**
 * Mock implementation of XMPPConnectionProvider
 * 
 * user0001-org01 -- Online, no special message set, already in roster
 *   
 * user0003-org01 -- Online, no special message set, not already in roster
 * 
 * @author jonnyheavey
 *
 */
public class MockXMPPConnectionProvider implements XMPPConnectionProvider {

	private XMPPUser user;		

	public MockXMPPConnectionProvider(XMPPUser user) {
		this.user=user;
	}
	
	@Override
	public Connection getConnection(XMPPConfiguration config, XMPPUser user) {
		
		Connection connection = new MockConnection(user.getUsername());
		
		try {
			connection.login(user.getUsername(), user.getPassword(), "");
		} catch (XMPPException e) {
			throw new XMPPExecutionException("Could not login as "+user, e);
		}
		
		return connection;
	}
	
	private static class MockConnection extends Connection {

		private String userName;
		
		protected MockConnection(String userName) {
			super(null);
			this.userName = userName;
		}

		@Override
		public String getUser() {
			return this.userName;
		}

		@Override
		public String getConnectionID() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isConnected() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isAuthenticated() {
			return true;
		}

		@Override
		public boolean isAnonymous() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isSecureConnection() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isUsingCompression() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void connect() throws XMPPException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void login(String username, String password, String resource)
				throws XMPPException {
			
			if(password.equals("invalidPassword")) {
				throw new XMPPException();
			}
			
		}

		@Override
		public void loginAnonymously() throws XMPPException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendPacket(Packet packet) {
			
			if(packet instanceof Presence) {
				if (!MockRoster.validUsers.contains(this.getUser())) {
					throw new RuntimeException("invalid user: "+this.getUser());
				}
				MockRoster.explicitPresences.put(this.getUser(), (Presence)packet);
			}
			
		}

		@Override
		public Roster getRoster() {
			return new MockRoster("user0001-org01");
		}
		
		@Override
		public String getServiceName() {
			return "";
		}

		@Override
		public void disconnect(Presence unavailablePresence) {
			// TODO Auto-generated method stub
			
		}
		
	}

	@Override
	public void closeConnection(XMPPConfiguration config, XMPPUser user) {
		// TODO Auto-generated method stub
	}

	@Override
	public void closeConnection(XMPPConfiguration config, XMPPUser user,
			Presence p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaultPresenceProvider(DefaultPresenceProvider provider) {
		// TODO Auto-generated method stub
		
	}

}
