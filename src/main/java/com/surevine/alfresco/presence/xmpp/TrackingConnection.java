package com.surevine.alfresco.presence.xmpp;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;

public class TrackingConnection {

	private boolean _hasBeenUsed=false;
	
	private Connection _connection;
	
	public TrackingConnection(Connection connection) {
		_connection=connection;
	}
	
	public Connection getSmackConnection() {
		_hasBeenUsed=true;
		return _connection;
	}
	
	public boolean hasBeenUsed() {
		return _hasBeenUsed;
	}
	
	public Connection getSmackConnectionSilently() {
		return _connection;
	}
	
}
