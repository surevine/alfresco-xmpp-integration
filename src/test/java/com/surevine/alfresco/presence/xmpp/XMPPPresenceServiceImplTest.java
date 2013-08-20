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

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.surevine.alfresco.presence.xmpp.SimplePresence.Availability;
import com.surevine.alfresco.presence.xmpp.XMPPPresenceServiceImpl.PresenceEntry;

import static org.junit.Assert.*;

public class XMPPPresenceServiceImplTest {
	
	protected XMPPPresenceServiceImpl fixture = new XMPPPresenceServiceImpl() {
		protected void setUpRoster() {
			setXMPPConnectionProvider(new MockXMPPConnectionProvider(new XMPPUser("user0002-org1", "validPassword")));
			super.setUpRoster();
		};
		protected UserSearchManager getUserSearchManager(Connection c) {
			return new MockUserSearchManager(c);
		}
	};
	
	@BeforeClass
	public static void setConfiguration() {
		XMPPConfiguration.getConfiguration().setHost("10.66.2.95");
	}
	
	protected SimplePresence runGetPresenceTest(String targetUserName) {
		SimplePresence presence = fixture.getPresence(targetUserName);
		return presence;
	}
	
	@Test
	public void testGetPresenceVanilla() {
		Map<String, PresenceEntry> resourceMap = new HashMap<String, PresenceEntry>();
		resourceMap.put("user0001-org01@10.66.2.95/testresource1", fixture.new PresenceEntry(new Presence(Type.available, "", 1, Mode.available)));

		fixture.presences.put("user0001-org01@10.66.2.95", resourceMap);
		SimplePresence presence = runGetPresenceTest("user0001-org01");
		//assertNotNull(presence);
		assertEquals(SimplePresence.Availability.AVAILABLE, presence.getAvailability());
	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testGetPresenceNullTargetUser() {
		SimplePresence presence = runGetPresenceTest(null);
	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testGetPresenceEmptyStringInTargetUser() {
		SimplePresence presence = runGetPresenceTest("");
	}
	
	@Test
	public void testGetPresenceUnknownTargetUser() {
		try {
			SimplePresence presence = runGetPresenceTest("MrMagoo");
			fail("An XMPPExecutionException was expected");
		}
		catch (XMPPExecutionException e) {
			if (!e.getMessage().contains("User does not exist: MrMagoo")) {
				fail("An exception was thrown, but it was not of the expected sort");
			}
		}
		catch (Exception e) {
			fail("An XMPPExecutionException was expected but a "+e.getClass()+" was thrown");
		}
	}
	
	@Test
	public void testGetPresenceTargetUserNotInRoster() {
		SimplePresence presence = runGetPresenceTest("user0004-org02");
	}	

	protected void runSetPresence(String userName, String password, SimplePresence.Availability availability, String superUserName, String superUserPassword) {
		XMPPUser user = new XMPPUser(userName, password);
		XMPPConnectionProvider connectionProvider = new MockXMPPConnectionProvider(user);
		fixture.setXMPPConnectionProvider(connectionProvider);
		
		SimplePresence sp = new SimplePresence(availability, user, "space", "message");

		Map<String, PresenceEntry> resourceMap = new HashMap<String, PresenceEntry>();
		resourceMap.put(userName+"@10.66.2.95/testresource1", fixture.new PresenceEntry(new Presence(Type.available, "", 1, sp.convertToXMPPPresence().getMode())));

		// Manually put presence into the collection, simulating the openfire presence events being captured
		fixture.presences.put(userName+"@10.66.2.95", resourceMap);
				
		fixture.setPresence(sp);
		
		XMPPConfiguration.getConfiguration().setSuperUserName(superUserName);
		XMPPConfiguration.getConfiguration().setSuperUserPassword(superUserPassword);

		assertEquals(availability, fixture.getPresence(user.getUsername()).getAvailability());		
	}
	
	@Test
	public void testSetPresenceOnline() {
		runSetPresence("user0001-org01", "T3G0gD1dJxp1sfko", SimplePresence.Availability.AVAILABLE, "user0002-org01", "T3G0gD1dJxp1sfko");
	}
	
	@Test
	public void testSetPresenceOnlineBusy() {
		runSetPresence("user0001-org01", "T3G0gD1dJxp1sfko", Availability.AVAILABLE, "user0002-org01", "T3G0gD1dJxp1sfko");
		runSetPresence("user0001-org01", "T3G0gD1dJxp1sfko", Availability.BUSY, "user0002-org01", "T3G0gD1dJxp1sfko");
	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testSetPresenceUnknownUser() {
		runSetPresence("MrEd", "T3G0gD1dJxp1sfko", Availability.AVAILABLE, "user0002-org01", "T3G0gD1dJxp1sfko");
	}
	
	@Test
	public void testSetPresenceNullsAndEmptyStrings() {
		try {
			runSetPresence("MrEd", "T3G0gD1dJxp1sfko", Availability.AVAILABLE, "user0002-org01", "T3G0gD1dJxp1sfko");
			fail("An exception was expected");
		} catch (XMPPExecutionException e) { }
		
		try {
			runSetPresence(null, "T3G0gD1dJxp1sfko", Availability.AVAILABLE, "user0002-org01", "T3G0gD1dJxp1sfko");
			fail("An exception was expected");
		} catch (XMPPExecutionException e) { }
		
		try {
			runSetPresence("MrEd", null, Availability.AVAILABLE, "user0002-org01", "T3G0gD1dJxp1sfko");
			fail("An exception was expected");
		} catch (XMPPExecutionException e) { }
		
		try {
			runSetPresence("MrEd", "T3G0gD1dJxp1sfko", null, "user0002-org01", "T3G0gD1dJxp1sfko");
			fail("An exception was expected");
		} catch (XMPPExecutionException e) { }
		
		try {
			runSetPresence("MrEd", "T3G0gD1dJxp1sfko", Availability.AVAILABLE, null, "T3G0gD1dJxp1sfko");
			fail("An exception was expected");
		} catch (XMPPExecutionException e) { }
		
		try {
			runSetPresence("MrEd", "T3G0gD1dJxp1sfko", Availability.AVAILABLE, "user0002-org01", null);
			fail("An exception was expected");
		} catch (XMPPExecutionException e) { }
		
		try {
			runSetPresence("", "T3G0gD1dJxp1sfko", Availability.AVAILABLE, "user0002-org01", "T3G0gD1dJxp1sfko");
			fail("An exception was expected");
		} catch (XMPPExecutionException e) { }
	}
	
}
