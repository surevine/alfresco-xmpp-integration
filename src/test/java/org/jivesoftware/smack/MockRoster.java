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
