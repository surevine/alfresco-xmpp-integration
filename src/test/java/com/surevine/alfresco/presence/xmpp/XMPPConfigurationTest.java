package com.surevine.alfresco.presence.xmpp;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;

public class XMPPConfigurationTest {
	
	protected XMPPConfiguration config = XMPPConfiguration.getConfiguration();
	
	private static String EXPECTED_DEFAULT_HOST = "10.66.2.95";
	private static int EXPECTED_DEFAULT_PORT = 5222;
	private static String NON_DEFAULT_HOST = "openfire.space.com";
	private static int NON_DEFAULT_PORT = 5223;

	@Test
	public void testGetConfigurationVanilla() {
		assertEquals(EXPECTED_DEFAULT_HOST, config.getHost());
		assertEquals(EXPECTED_DEFAULT_PORT, config.getPort());
	}
	
	@Test
	public void testSetHostVanilla() {
		config.setHost(NON_DEFAULT_HOST);
		assertEquals(NON_DEFAULT_HOST, config.getHost());	
	}
	
	@Test
	public void testSetPortVanilla() {
		config.setPort(NON_DEFAULT_PORT);
		assertEquals(NON_DEFAULT_PORT, config.getPort());	
	}
	
	@Test(expected=XMPPConfigurationException.class)
	public void testSetNullHost() {
		config.setHost(null);
	}
	
	@Test(expected=XMPPConfigurationException.class)
	public void testSetEmptyHost() {
		config.setHost("");
	}
	
	@After
	public void tidyConfiguration() {
		config.setHost("10.66.2.95");
		config.setPort(5222);
	}
	
}
