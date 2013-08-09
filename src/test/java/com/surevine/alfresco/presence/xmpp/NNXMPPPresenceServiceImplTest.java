package com.surevine.alfresco.presence.xmpp;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;
import org.junit.Test;

import com.surevine.alfresco.presence.xmpp.SimplePresence.Availability;
import com.surevine.alfresco.presence.xmpp.XMPPPresenceServiceImpl.PresenceEntry;

public class NNXMPPPresenceServiceImplTest {

	static {
		if (System.getProperty("com.surevine.openfire.host")!=null) {
			XMPPConfiguration.getConfiguration().setHost(System.getProperty("com.surevine.openfire.host"));
		}
	}
	
	boolean testIsRosterSetup=false;
	protected XMPPPresenceServiceImpl fixture = new XMPPPresenceServiceImpl() {
		protected void setUpRoster() {
			if (!isRosterSetup){
				XMPPConfiguration.getConfiguration().setSuperUserName("user0002-org01");
				XMPPConfiguration.getConfiguration().setSuperUserPassword("T3G0gD1dJxp1sfko");
				if (!testIsRosterSetup) {
					setXMPPConnectionProvider(new XMPPConnectionProviderImpl());
					testIsRosterSetup=true;
				}
			}
			super.setUpRoster();
		};		
	};
	
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
		// TODO need to fix to be throwing exception for the correct reason... (that user doesn't exist...)
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
		SimplePresence presence = runGetPresenceTest("user0009-org02");
	}	

	// TODO add tests for if superuser not setup correctly...
	// Can we instantiate impl fresh for each test? even if its singleton?
	//

	protected void runSetPresence(String userName, String password, SimplePresence.Availability availability, String superUserName, String superUserPassword) {
		XMPPUser user = new XMPPUser(userName, password);
		XMPPConnectionProvider connectionProvider = new MockXMPPConnectionProvider(user);
		fixture.setXMPPConnectionProvider(connectionProvider);
		
		SimplePresence sp = new SimplePresence(availability, user, "space", "message");

		// Manually put presence into the collection, simulating the openfire presence events being captured
		Map<String, PresenceEntry> resourceMap = new HashMap<String, PresenceEntry>();
		resourceMap.put(userName+"@10.66.2.95/testresource1", fixture.new PresenceEntry(new Presence(Type.available, "", 1, sp.convertToXMPPPresence().getMode())));
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