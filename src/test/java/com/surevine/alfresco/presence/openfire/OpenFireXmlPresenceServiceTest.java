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
package com.surevine.alfresco.presence.openfire;

import java.io.StringReader;
import java.net.URI;

import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import com.surevine.alfresco.presence.Availability;
import com.surevine.alfresco.presence.Presence;
import com.surevine.alfresco.presence.openfire.OpenFireXmlPresenceService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.any;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the OpenFireXmlPresenceService against various simulated presence API responses under different caching configurations
 * 
 * @see <a href="http://www.igniterealtime.org/projects/openfire/plugins/presence/readme.html">Presence Plugin Readme</a>
 * @see <a href="http://xmpp.org/rfcs/rfc3921.html">RFC3921 - XMPP: Instant Messaging and Presence</a>
 * 
 * @author Paul Guare
 */
public class OpenFireXmlPresenceServiceTest {

	@Test
	public void testChatWithStatus()  {
		assertEquals(Availability.ONLINE, getStatusForResponse("<presence from=\"someone@example.org/emite-1344518641302\"><show>chat</show><status>Custom Status</status></presence>").getAvailability());
	}
	
	@Test
	public void testChatWithoutStatus()  {
		assertEquals(Availability.ONLINE , getStatusForResponse("<presence from=\"someone@example.org/emite-1344518641302\"><show>chat</show></presence>").getAvailability());		
	}
	
	@Test
	public void testNoShowWithoutStatus()  {
		assertEquals(Availability.ONLINE , getStatusForResponse("<presence from=\"someone@example.org/emite-1344518641302\"></presence>").getAvailability());
	}
	
	@Test
	public void testNoShowWithUnavailableStatus()  {
		assertEquals(Availability.OFFLINE , getStatusForResponse("<presence type=\"unavailable\" from=\"someone@example.org\"><status>Unavailable</status></presence>").getAvailability());
	}
	
	@Test
	public void testDnD()  {
		assertEquals(Availability.BUSY ,  getStatusForResponse("<presence from=\"someone@example.org/emite-1344518641302\"><show>dnd</show></presence>").getAvailability());
	}
	
	@Test
	public void testAway()  {
		assertEquals(Availability.AWAY , getStatusForResponse("<presence from=\"someone@example.org/emite-1344518641302\"><show>away</show></presence>").getAvailability());
	}
	
	@Test
	public void testXa()  {
		assertEquals(Availability.AWAY , getStatusForResponse("<presence from=\"someone@example.org/emite-1344518641302\"><show>xa</show></presence>").getAvailability());
	}
	
	@Test
	public void testDnDWithStatus()  {
		assertEquals(Availability.BUSY , getStatusForResponse("<presence from=\"someone@example.org/emite-1344518641302\"><show>dnd</show><status>Custom status</status></presence>").getAvailability());
	}
	
	/**
	 * Service changed - no longer uses exceptions but rather returns Availability.UNKNOWN if unable to retrieve status.
	 */
	@Test
	public void testErrorResponse()  {
		Presence presence =  getStatusForResponse("<presence type=\"error\" from=\"nonexistantuser@example.org\"><error code=\"403\" type=\"auth\"><forbidden xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"/></error></presence>");
		assertNotNull(presence);
		assertEquals(Availability.UNKNOWN , presence.getAvailability());
		assertNotNull(presence.getStatus());
		assertEquals("example.org",presence.getHost());
		assertEquals("someone", presence.getUserName());
		assertEquals("someone@example.org",presence.getJid());
	}
	
	
	/**
	 * Both valid and invalid presence calls should return false for Presence#isServiceEnabled when its not set
	 */
	@Test
	public void testServiceUnreachable() {
		OpenFireXmlPresenceService service = new OpenFireXmlPresenceService();
		service.setHost("example.org");
		service.setUrl("http://example.org/presence");
		
		service.setRemoteServiceEnabled(false);
		
		Presence presence = getStatusForResponse(service, "<presence from=\"someone@example.org/emite-1344518641302\"><show>dnd</show><status>Custom status</status></presence>");
		
		assertEquals(Availability.UNKNOWN,presence.getAvailability());
		assertFalse(presence.isServiceEnabled());
		
		presence =  getStatusForResponse(service,"<presence type=\"error\" from=\"nonexistantuser@example.org\"><error code=\"403\" type=\"auth\"><forbidden xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"/></error></presence>");
		assertFalse(presence.isServiceEnabled());
		
	}
	
	/**
	 * Both valid and invalid presence calls should return true for Presence#isServiceEnabled when its set
	 */
	@Test
	public void testServiceReachable() {
		OpenFireXmlPresenceService service = new OpenFireXmlPresenceService();
		service.setHost("example.org");
		service.setUrl("http://example.org/presence");
	
		service.setRemoteServiceEnabled(true);
		
		Presence presence = getStatusForResponse(service, "<presence from=\"someone@example.org/emite-1344518641302\"><show>dnd</show><status>Custom status</status></presence>");
		
		assertEquals(Availability.BUSY,presence.getAvailability());
		assertTrue(presence.isServiceEnabled());
		
		presence =  getStatusForResponse(service,"<presence type=\"error\" from=\"nonexistantuser@example.org\"><error code=\"403\" type=\"auth\"><forbidden xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"/></error></presence>");
		assertTrue(presence.isServiceEnabled());
		
	}
	
	@Test
	public void testCacheTimeout() throws  InterruptedException {
		OpenFireXmlPresenceService service = new OpenFireXmlPresenceService();
		service.setHost("example.org");
		service.setUrl("http://example.org/presence");

		// Cache presence info for 5 seconds
		service.setCacheTimeout(5000L);
		
		assertEquals(Availability.BUSY,getStatusForResponse(service, "<presence from=\"someone@example.org/emite-1344518641302\"><show>dnd</show><status>Custom status</status></presence>").getAvailability());

		// This response would indicate 'ONLINE', but we should get the cached presence info from before
		assertEquals(Availability.BUSY, getStatusForResponse(service, "<presence from=\"someone@example.org/emite-1344518641302\"></presence>").getAvailability());
		
		// Wait 6 seconds
		Thread.sleep(6000L);

		// By now the cache should have expired, so we get the expected availability state
		assertEquals(Availability.ONLINE,getStatusForResponse(service, "<presence from=\"someone@example.org/emite-1344518641302\"></presence>").getAvailability());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testErrorTimeout() throws InterruptedException {
		OpenFireXmlPresenceService service = new OpenFireXmlPresenceService();
		service.setHost("example.org");
		service.setUrl("http://example.org/presence");
		
		// Wait 5 seconds after a network error before issuing any more requests
		service.setErrorTimeout(5000L);
		
		RestOperations rest = mock(RestOperations.class);
		when(rest.getForObject(any(URI.class), any(Class.class))).thenThrow(RestClientException.class);
		
		service.setRestOperations(rest);
		
		service.getUserPresence("someone");
		service.getUserPresence("someone");
		
		// We should only have invoked the REST API once because the first attempt threw an error
		verify(rest, times(1)).getForObject(any(URI.class), any(Class.class));
		
		// Wait 6 seconds
		Thread.sleep(6000L);
		
		service.getUserPresence("someone");
	
		
		// By now the timeout interval should have expired so the third attempt should have triggered a second REST call
		verify(rest, times(2)).getForObject(any(URI.class), any(Class.class));
		
	}
	
	private Presence getStatusForResponse(String response)  {
		return getStatusForResponse(null, response);
	}
	
	@SuppressWarnings("unchecked")
	private Presence getStatusForResponse(OpenFireXmlPresenceService service, String response)  {
		RestOperations rest = mock(RestOperations.class);
		when(rest.getForObject(any(URI.class), any(Class.class))).thenReturn(new StreamSource(new StringReader(response)));
		if (service == null) {
			service = new OpenFireXmlPresenceService();
			service.setHost("example.org");
			service.setUrl("http://example.org/presence");
		}
		service.setRestOperations(rest);
		return service.getUserPresence("someone");
	}

}
