package com.surevine.alfresco.presence.xmpp;

import static org.junit.Assert.*;

import org.junit.Test;

public class XMPPUserTest {
	
	protected String VALID_USERNAME = "testUser";
	protected String VALID_PASSWORD = "testPassword";

	@Test
	public void testCreateUserVanilla() {
		
		XMPPUser user = new XMPPUser(VALID_USERNAME, VALID_PASSWORD);
		
		assertEquals(VALID_USERNAME, user.getUsername());
		assertEquals(VALID_PASSWORD, user.getPassword());

	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testCreateUserNullUsername() {
		XMPPUser user = new XMPPUser(null, VALID_PASSWORD);
	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testCreateUserEmptyUsername() {
		XMPPUser user = new XMPPUser("", VALID_PASSWORD);
	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testCreateUserNullPassword() {
		XMPPUser user = new XMPPUser(VALID_USERNAME, null);
	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testCreateUserEmptyPassword() {
		XMPPUser user = new XMPPUser(VALID_USERNAME, "");
	}
	
}
