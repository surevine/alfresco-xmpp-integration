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
