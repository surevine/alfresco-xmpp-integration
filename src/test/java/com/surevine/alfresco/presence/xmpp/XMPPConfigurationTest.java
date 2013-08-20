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
