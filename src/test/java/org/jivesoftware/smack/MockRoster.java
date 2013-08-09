package org.jivesoftware.smack;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;

/**
 * Mock implementation of Roster
 * 
 * @author jonnyheavey
 *
 */
public class MockRoster	extends Roster {
	
	public static final Collection<String> validUsers;
	
	public static final Map<String, Presence> explicitPresences = new HashMap<String, Presence>();
	static {
		explicitPresences.put("user0001-org01", new Presence(Type.available, "", 1, Mode.available));
		explicitPresences.put("user0003-org01", new Presence(Type.available, "", 1, Mode.available));
		explicitPresences.put("superUser", new Presence(Type.available, "", 1, Mode.available));

		Set<String> validUsersS = explicitPresences.keySet();
		validUsers = validUsersS;
	}
	
	Collection<String> usersInRoster;
	
	public MockRoster(String... usersInRoster) {
		super(new Connection(new ConnectionConfiguration("test")) {

			@Override
			public String getUser() {
				// TODO Auto-generated method stub
				return null;
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
				// TODO Auto-generated method stub
				return false;
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
				// TODO Auto-generated method stub
				
			}

			@Override
			public void loginAnonymously() throws XMPPException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void sendPacket(Packet packet) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public Roster getRoster() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void disconnect(Presence unavailablePresence) {
				// TODO Auto-generated method stub
				
			}
			
		});
		this.usersInRoster= Arrays.asList(usersInRoster);
	}
	
	public boolean contains(String userName) {
		return usersInRoster.contains(userName);
	}
	
	public void createEntry(String s1, String s2, String[] s3Arr) {
		//Do nothing
	}
	
	public Presence getPresence(String userName) {
		System.out.println("In Mock!");
		Presence explicit = explicitPresences.get(userName);
		if (explicit == null) {
			throw new RuntimeException("Unknown user!");
		}
		return explicit;
	}
	
}
