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

import java.util.Iterator;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smackx.OfflineMessageHeader;
import org.jivesoftware.smackx.OfflineMessageManager;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.junit.Before;
import org.junit.Test;

import com.surevine.alfresco.presence.xmpp.SimplePresence.Availability;

public class XMPPUnreadMessageServiceImplTest {
	
	protected XMPPUnreadMessageServiceImpl fixture = new XMPPUnreadMessageServiceImpl() {
		
		protected OfflineMessageManager getOfflineMessageManager(Connection connection) {
			return new MockOfflineMessageManager(connection);
		}
		protected UserSearchManager getUserSearchManager(Connection c) {
			return new MockUserSearchManager(c);
		}
		
	};
	
	protected XMPPPresenceServiceImpl presenceService = new XMPPPresenceServiceImpl() {
		protected void setUpRoster() {
			setXMPPConnectionProvider(new MockXMPPConnectionProvider());
			super.setUpRoster();
		};
		protected UserSearchManager getUserSearchManager(Connection c) {
			return new MockUserSearchManager(c);
		}
	};
	
	private static int EXPECTED_MESSAGE_COUNT = 1;
	private static int EXPECTED_MESSAGE_HEADER_COUNT = 1;
	
	protected int runGetUnreadMessageCount(XMPPUser user) {
		int unreadMessageCount = fixture.getUnreadMessageCount(user);
		return unreadMessageCount;
	}
	
	protected Iterator<OfflineMessageHeader> runGetUnreadMessageHeaders(XMPPUser user) {
		Iterator<OfflineMessageHeader> unreadMessageHeaders = fixture.getUnreadMessageHeaders(user);
		return unreadMessageHeaders;
	}
	
	protected Iterator<Message> runGetUnreadMessages(XMPPUser user) {
		Iterator<Message> unreadMessages = fixture.getUnreadMessages(user);
		return unreadMessages;
	}
	
	@Before
	public void setMockConnectionProvider() {
		fixture.setXMPPConnectionProvider(new MockXMPPConnectionProvider());
	}
	
	@Test
	public void testGetUnreadMessageCountVanilla() {
		int messageCount = runGetUnreadMessageCount(new XMPPUser("user0001-org01", "validPassword"));
		assertEquals(EXPECTED_MESSAGE_COUNT, messageCount);
	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testGetUnreadMessageCountNullUser() {
		runGetUnreadMessageCount(null);
	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testGetUnreadMessageCountUnknownUser() {
		runGetUnreadMessageCount(new XMPPUser("MrMagoo", "validPassword"));
	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testGetUnreadMessageCountInvalidPassword() {
		runGetUnreadMessageCount(new XMPPUser("user0001-org01", "invalidPassword"));
	}

	@Test
	public void testGetUnreadMessageHeadersVanilla() {
		Iterator<OfflineMessageHeader> unreadMessages = runGetUnreadMessageHeaders(new XMPPUser("user0001-org01", "validPassword"));
		
		assertNotNull(unreadMessages);
		
		int messageHeaderCount = 0;
		while(unreadMessages.hasNext()) {
			messageHeaderCount++;
			unreadMessages.next();
		}
		
		assertEquals(EXPECTED_MESSAGE_HEADER_COUNT, messageHeaderCount);

	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testGetUnreadMessageHeadersNullUser() {
		runGetUnreadMessageHeaders(null);
	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testGetUnreadMessageHeadersUnknownUser() {
		runGetUnreadMessageHeaders(new XMPPUser("MrMagoo", "validPassword"));
	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testGetUnreadMessageHeadersInvalidPassword() {
		runGetUnreadMessageHeaders(new XMPPUser("user0001-org01", "invalidPassword"));
	}
	
	@Test
	public void testGetUnreadMessagesVanilla() {
		Iterator<Message> unreadMessages = runGetUnreadMessages(new XMPPUser("user0001-org01", "validPassword"));
		
		assertNotNull(unreadMessages);
		
		int messageHeaderCount = 0;
		while(unreadMessages.hasNext()) {
			messageHeaderCount++;
			unreadMessages.next();
		}
		
		assertEquals(EXPECTED_MESSAGE_COUNT, messageHeaderCount);

	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testGetUnreadMessagesNullUser() {
		runGetUnreadMessages(null);
	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testGetUnreadMessagesUnknownUser() {
		runGetUnreadMessages(new XMPPUser("MrMagoo", "validPassword"));
	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testGetUnreadMessagesInvalidPassword() {
		runGetUnreadMessages(new XMPPUser("user0001-org01", "invalidPassword"));
	}
	
	
	@Test
	public void testValidateUserOK() {
		XMPPUser user = new XMPPUser("user0001-org01", "password");
		fixture.validateUser(user);
	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testValidateUserNullUser() {
		fixture.validateUser(null);
	}
	
	@Test
	public void testValidateUserAssociatedDodgyUsers() {
		try {
			XMPPUser user = new XMPPUser(null, "password");
			fixture.validateUser(user);
			fail("An exception was expected, but none occured");
		}
		catch (XMPPExecutionException e) { }
		catch (Exception e) {
			fail("An XMPPExecutionException was expected, but instead a "+e.getClass()+" was thrown");
		}
		
		try {
			XMPPUser user = new XMPPUser("user0001-org01", null);
			fixture.validateUser(user);
			fail("An exception was expected, but none occured");
		}
		catch (XMPPExecutionException e) { }
		catch (Exception e) {
			fail("An XMPPExecutionException was expected, but instead a "+e.getClass()+" was thrown");
		}
		
		try {
			XMPPUser user = new XMPPUser("", "password");
			fixture.validateUser(user);
			fail("An exception was expected, but none occured");
		}
		catch (XMPPExecutionException e) { }
		catch (Exception e) {
			fail("An XMPPExecutionException was expected, but instead a "+e.getClass()+" was thrown");
		}
		
		try {
			XMPPUser user = new XMPPUser("user0001-org01", "");
			fixture.validateUser(user);
			fail("An exception was expected, but none occured");
		}
		catch (XMPPExecutionException e) { }
		catch (Exception e) {
			fail("An XMPPExecutionException was expected, but instead a "+e.getClass()+" was thrown");
		}
	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testValidateUserDoesntExist() {
		XMPPUser user = new XMPPUser("IDONTEXIST-org01", "password");
		fixture.validateUser(user);
	}
}
