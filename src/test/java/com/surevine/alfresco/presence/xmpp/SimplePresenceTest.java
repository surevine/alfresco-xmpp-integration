package com.surevine.alfresco.presence.xmpp;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;
import org.junit.Test;

import com.surevine.alfresco.presence.xmpp.SimplePresence.Availability;

import static org.junit.Assert.*;

public class SimplePresenceTest {

	
	protected void runPresenceConversionTest (Presence.Type type, Presence.Mode mode, SimplePresence.Availability availability) {
		Presence p = new Presence(type, "", 0, mode);
		assertEquals(availability, SimplePresence.extractAvailability(p));
	}
	
	protected void runSourceTest(int priority, String expectedSource, String from) {
		Presence p = new Presence(Presence.Type.available);
		p.setPriority(priority);
		p.setFrom(from);
		assertEquals(expectedSource, SimplePresence.extractSource(p));
	}
	
	protected void runUserNameTest(String jid, String expectedUserName) {
		Presence p = new Presence(Presence.Type.available);
		p.setFrom(jid);
		assertEquals(expectedUserName, SimplePresence.extractUserName(p));
	}
	
	protected void runSimpleToXMPPTest(SimplePresence sp, String expectedFrom, Presence.Type expectedType, Presence.Mode expectedMode, int expectedPriority, String expectedMessage) {
		Presence p = sp.convertToXMPPPresence();
		assertEquals(expectedFrom, p.getFrom());
		assertEquals(expectedType, p.getType());
		assertEquals(expectedMode, p.getMode());
		assertEquals(expectedPriority, p.getPriority());
		assertEquals(expectedMessage, p.getStatus());
	}
	
	@Test
	public void testPresenceUnavailableTypeNullMode() {
		runPresenceConversionTest(Presence.Type.unavailable, null, SimplePresence.Availability.OFFLINE);
	}
	
	@Test
	public void testPresenceUnavailableTypeModeAvailable() {
		runPresenceConversionTest(Presence.Type.unavailable, Presence.Mode.available, SimplePresence.Availability.OFFLINE);
	}
	
	@Test
	public void testPresenceUnavailableTypeModeAway() {
		runPresenceConversionTest(Presence.Type.unavailable, Presence.Mode.away, SimplePresence.Availability.OFFLINE);
	}
	
	@Test
	public void testPresenceUnavailableTypeModeChat() {
		runPresenceConversionTest(Presence.Type.unavailable, Presence.Mode.chat, SimplePresence.Availability.OFFLINE);
	}
	
	@Test
	public void testPresenceUnavailableTypeModeDND() {
		runPresenceConversionTest(Presence.Type.unavailable, Presence.Mode.dnd, SimplePresence.Availability.OFFLINE);
	}
	
	@Test
	public void testPresenceUnavailableTypeModeXA() {
		runPresenceConversionTest(Presence.Type.unavailable, Presence.Mode.xa, SimplePresence.Availability.OFFLINE);
	}
	
	@Test
	public void testPresenceUnavailableTypeModeNull() {
		runPresenceConversionTest(Presence.Type.unavailable, null, SimplePresence.Availability.OFFLINE);
	}
	
	@Test
	public void testPresenceAvailableTypeModeAvailable() {
		runPresenceConversionTest(Presence.Type.available, Presence.Mode.available, SimplePresence.Availability.AVAILABLE);
	}
	
	@Test
	public void testPresenceAvailableTypeModeAway() {
		runPresenceConversionTest(Presence.Type.available, Presence.Mode.away, SimplePresence.Availability.BUSY);
	}
	
	@Test
	public void testPresenceAvailableTypeModeChat() {
		runPresenceConversionTest(Presence.Type.available, Presence.Mode.chat, SimplePresence.Availability.AVAILABLE);
	}
	
	@Test
	public void testPresenceAvailableTypeModeDND() {
		runPresenceConversionTest(Presence.Type.available, Presence.Mode.dnd, SimplePresence.Availability.BUSY);
	}
	
	@Test
	public void testPresenceAvailableTypeModeXA() {
		runPresenceConversionTest(Presence.Type.available, Presence.Mode.xa, SimplePresence.Availability.BUSY);
	}
	
	@Test
	public void testPresenceAvailableTypeModeNull() {
		runPresenceConversionTest(Presence.Type.available, null, SimplePresence.Availability.AVAILABLE);
	}
	
	@Test
	public void testExtractSourceSpace() {
		runSourceTest(-1, "space", "Smack-Space-12345@678910");
	}
	
	@Test
	public void testExtractSourceChat() {
		runSourceTest(1, "chat", "emite-1234@56");
		runSourceTest(0, "chat", "emite-1234@56");
		runSourceTest(-128, "chat", "emite");
		runSourceTest(128, "chat", "emite-1234");
	}
	
	@Test
	public void testExtractUsernameVanilla() {
		runUserNameTest("simon@127.0.0.1/12345asdfg", "simon");
	}
	
	@Test
	public void testExtractUsernameNullJid() {
		runUserNameTest(null, "unknown");
	}
	
	@Test
	public void testExtractUsernameNoResourcePart() {
		runUserNameTest("simon@127.0.0.1/", "simon");
		runUserNameTest("simon@127.0.0.1", "simon");
	}
	
	@Test
	public void testExtractUsernameNoJidButHasResource() {
		runUserNameTest("simon@/12345asdfg", "simon");
		runUserNameTest("simon/12345asdfg", "simon");
	}
	
	@Test
	public void testExtractUsernameJustAUsername() {
		runUserNameTest("simon", "simon");
	}
	
	@Test
	public void testExtractUsernameUnexpectedDelimiters() {
		runUserNameTest("simon@1/@2//@7.0//.@0.1/@1@///34@5a/sdfg", "simon");
	}
	
	@Test
	public void testOfflineToXMPP() {
		SimplePresence sp = new SimplePresence(Availability.OFFLINE, new XMPPUser("simonw", "password"), "space", "Custom Message");
		runSimpleToXMPPTest(sp, "simonw@unknown/space", Type.unavailable, Mode.xa, -1, "Custom Message" );
	}
	
	@Test
	public void testAvailableToXMPP() {
		SimplePresence sp = new SimplePresence(Availability.AVAILABLE, new XMPPUser("simonw", "password"), "space", "");
		runSimpleToXMPPTest(sp, "simonw@unknown/space", Type.available, Mode.available, -1, "" );
	}
	
	@Test
	public void testBusyToXMPP() {
		SimplePresence sp = new SimplePresence(Availability.BUSY, new XMPPUser("simo______nw","a"), "space", null);
		runSimpleToXMPPTest(sp, "simo______nw@unknown/space", Type.available, Mode.dnd, -1, "" );
	}
	
	@Test
	public void testChatPresenceToXMPP() {
		SimplePresence sp = new SimplePresence(Availability.AVAILABLE, new XMPPUser("simo______nw","a"), "chat", "              LO          NG       MESSAGE          ");
		runSimpleToXMPPTest(sp, "simo______nw@unknown/emite", Type.available, Mode.available, 0, "              LO          NG       MESSAGE          " );
	}
	
	@Test
	public void testUnknownSourceToXMPP() {
		SimplePresence sp = new SimplePresence(Availability.AVAILABLE, new XMPPUser("simo______nw","a"), "somethingCrazy!", "              LO          NG       MESSAGE          ");
		runSimpleToXMPPTest(sp, "simo______nw@unknown/Unknown", Type.available, Mode.available, 0, "              LO          NG       MESSAGE          " );
	}
	
	@Test
	public void testSimplePresenceFromNull() {
		SimplePresence sp = SimplePresence.fromSmackPresence(null, "myuser");
		assertEquals("", sp.getMessage());
		assertEquals("offline", sp.getSource());
		assertEquals("myuser", sp.getUserName());
	}
	
	
	
	
	
	
	
}
