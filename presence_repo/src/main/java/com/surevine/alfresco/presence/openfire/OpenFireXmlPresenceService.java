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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.xml.xpath.Jaxp13XPathTemplate;
import org.springframework.xml.xpath.XPathOperations;
import org.w3c.dom.Node;

import com.surevine.alfresco.presence.Presence;
import com.surevine.alfresco.presence.PresenceException;
import com.surevine.alfresco.presence.PresenceService;
import com.surevine.alfresco.presence.Availability;

/**
 * Provides services for requesting presence information from a remote Openfire server (with the presence plugin installed)
 * via its XML REST API
 * 
 * @see <a href="http://www.igniterealtime.org/projects/openfire/plugins/presence/readme.html">Presence Plugin Readme</a>
 * @see <a href="http://xmpp.org/rfcs/rfc3921.html">RFC3921 - XMPP: Instant Messaging and Presence</a>
 * 
 * @author Paul Guare
 *
 */
public class OpenFireXmlPresenceService implements PresenceService {

	private static final Log _logger = LogFactory.getLog(OpenFireXmlPresenceService.class);

	/** URL for the presence REST service **/	
	private String url;
	
	/** Appended to usernames to form complete Jabber IDs (JIDs) of the form <username>@<host>, e.g. jsmith@example.com **/ 
	private String host;	
	
	/** The number of milliseconds to wait after a network error before we attempt any further requests **/
	private long errorTimeout = 0;	
	
	/** The number of milliseconds we cache remote presence responses for a given user **/
	private long cacheTimeout = 0;
	
	/** The time the last network error took place. Used to support the error timeout feature **/
	private long lastErrorTimestamp;
	
	/** If set to false then no remote presence requests will be sent **/
	private boolean remoteServiceEnabled = true;
	
	/** Maintains a local cache of presence information, indexed by user name **/
	private Map<String, PresenceCacheEntry> presenceCache = Collections.synchronizedMap(new HashMap<String, PresenceCacheEntry>());
	
	/** Used to query the remote Openfire server **/
	private RestOperations restOperations;
	
	/** Used to extract presence information from the REST responses **/
	private XPathOperations xpathTemplate = new Jaxp13XPathTemplate();

	/**
	 * Requests presence information for a specified user.
	 * 
	 * @param userName ID of the user whose presence information we are requesting.
	 */
	@Override
	public Presence getUserPresence(final String userName)  {
		
		Presence defaultFailedPresence = new Presence(Availability.UNKNOWN, "", userName, getHost(),isRemoteServiceEnabled());
		Presence presence = null;
		
		try 
		{
			
			if (isCacheEnabled() && getCachedPresence(userName)!=null) {
				_logger.info("Returning cached presence information for user " + userName);
				presence = getCachedPresence(userName);
			}	else  if (isInErrorTimeout()) {
				_logger.info("Not requesting presence information for user " + userName + " as we are in an error timeout interval");
				presence = defaultFailedPresence;
			} else if (isRemoteServiceEnabled()) {
				_logger.info("Requesting remote presence information for user " + userName + " on host " + getHost());

				Presence remotePresence = requestPresence(userName);
				
				if (remotePresence!=null && isCacheEnabled())
				{
					cachePresence(userName, remotePresence);
				}
				
				presence = remotePresence;
			} else {
				_logger.info("Not requesting presence information for user " + userName + " as remote presence requests are disabled");
				presence = defaultFailedPresence;
			}
			
			
		} catch (PresenceException e) {
			// Allways ensure a presence is returned - even if Availability is UNKNOWN
			_logger.error(e);
			presence = defaultFailedPresence;
		}
		
		return presence;
	}
	
	/**
	 * Is local caching enabled?
	 * 
	 * @return true if local caching is enabled, false otherwise
	 */
	private boolean isCacheEnabled() {
		return cacheTimeout > 0;
	}
	
	/**
	 * Attempts to retrieve presence information for the specified user from the local cache.
	 * If such information exists (and is it has not expired), it is returned to the caller.
	 * 
	 * @param userName ID of the user whose presence info we are requesting
	 * @return Presence information for the specified user from the local cache
	 */
	private Presence getCachedPresence(String userName) {
		PresenceCacheEntry cachedPresence = presenceCache.get(userName);
		if (cachedPresence != null && !cachedPresence.isExpired()) {
			return cachedPresence.getPresence();
		} else {
			return null;
		}
	}
	
	/**
	 * Updates the local cache with the specified presence information for the specified user
	 * 
	 * @param userName ID of the user whose presence information we are caching 
	 * @param presence Presence information
	 */
	private void cachePresence(final String userName, final Presence presence) {
		presenceCache.put(userName, new PresenceCacheEntry(presence));
	}

	/**
	 * Requests presence information for the specified user from the remote Openfire service via its
	 * XML REST API
	 * 
	 * @param userName ID of the user whose presence info we are requesting
	 * @return Presence info for the requested user
	 * @throws PresenceException
	 */
	private Presence requestPresence(final String userName) throws PresenceException {
		_logger.info("Requesting presence information for user " + userName);

		Source response;

		try {
			response = restOperations.getForObject(getRequestUri(userName), Source.class);
		} catch (RestClientException e) {
			startErrorTimeout();
			throw new PresenceException(e);
		} catch (URISyntaxException e) {
			throw new PresenceException(e);
		} catch (UnsupportedEncodingException e) {
			throw new PresenceException(e);
		}

		_logger.info("Retrieved presence information for user " + userName);
	
		return parsePresenceResponse(response,userName);
	}
	
	/**
	 * Generates a REST URI that will retrieve presence information for the specified user from a remote 
	 * Openfire server in XML format
	 * 
	 * @param userName 
	 * @return 
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 */
	private URI getRequestUri(String userName) throws URISyntaxException, UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();

		sb.append(url).append("?jid=")
				.append(URLEncoder.encode(userName, "utf-8")).append("%40")
				.append(URLEncoder.encode(host, "utf-8")).append("&type=xml");

		return new URI(sb.toString());
	}

	/**
	 * Parses the XML response from the remote Openfire server and extracts presence information
	 * 
	 * @see <a href="http://xmpp.org/rfcs/rfc3921.html">RFC3921 - XMPP: Instant Messaging and Presence</a>
	 * 
	 * @param response The XML response from the remote Openfire server
	 * @param userName 
	 * @return Presence information extracted from the specified response
	 * @throws PresenceException
	 */
	private Presence parsePresenceResponse(Source response, String userName)	throws PresenceException {
		Node presence = xpathTemplate.evaluateAsNode("/", response);
		Source source = new DOMSource(presence);

		String type = xpathTemplate.evaluateAsString("/presence/@type", source);
		String show = xpathTemplate.evaluateAsString("/presence/show", source);
		String status = xpathTemplate.evaluateAsString("/presence/status", source);

		Availability availability;
		
		if (type.equals("error")) {
			String errorType = xpathTemplate.evaluateAsString("/presence/error/@type", source);
			throw new PresenceException("Received error type: " + errorType);
		} else if ((type.equals("") && (show.equals("")) || show.equals("chat"))) {
			availability = Availability.ONLINE;
		} else if (show.equals("away") || show.equals("xa")) {
			availability = Availability.AWAY;
		} else if (show.equals("dnd")) {
			availability = Availability.BUSY;
		} else {
			availability = Availability.OFFLINE;
		}

		return new Presence(availability, status, userName, getHost(),true);
	}
	
	/**
	 *  Enter a timeout interval following a network error retrieving presence information.
	 *  During this time we should not attempt any further requests.
	 */
	private void startErrorTimeout() {
		lastErrorTimestamp = System.currentTimeMillis();
	}
	
	/**
	 * Checks whether we are still in a timeout interval following a network error retrieving presence
	 * information. During this time we should not attempt any further requests.
	 * 
	 * @return true if we should not yet attempt any more remote presence requests
	 */
	private boolean isInErrorTimeout() {
		return errorTimeout != 0 && !(System.currentTimeMillis() >= lastErrorTimestamp + errorTimeout);
	}
	
	/**
	 * @return The URL for the presence REST service
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the URL for the presence REST service
	 * 
	 * @param url URL for the presence REST service
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return The hostname or IP address we append to user names to create full Jabber IDS (JIDS)
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Sets the hostname (or IP address) that shoud be appended to usernames to form complete Jabber IDs (JIDs) 
	 * of the form <username>@<host>, e.g. jsmith@example.com 
	 * 
	 * @param host Hostname or IP address to append to user names to create full Jabber IDS (JIDS)
	 */ 
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return The number of milliseconds we wait after a network error before we attempt any further requests
	 */
	public long getErrorTimeout() {
		return errorTimeout;
	}
	
	/**
	 * Sets the number of milliseconds to wait after a network error before we attempt any further requests
	 * 
	 * @param errorTimeoutMillis the number of milliseconds to wait after a network error before we attempt any further requests
	*/
	public void setErrorTimeout(long errorTimeout) {
		this.errorTimeout = errorTimeout;
	}

	/**
	 * @return The number of milliseconds we cache remote presence responses for a given user
	 */
	public long getCacheTimeout() {
		return cacheTimeout;
	}

	/**
	 * Sets the number of milliseconds we cache remote presence responses for a given user
	 * 
	 * @param cacheTimeoutMillis the number of milliseconds we cache remote presence responses for a given user
	 */
	public void setCacheTimeout(long cacheTimeout) {
		this.cacheTimeout = cacheTimeout;
	}

	/**
	 * @return true if remote presence requests are enabled, false otherwise
	 */
	public boolean isRemoteServiceEnabled() {
		return remoteServiceEnabled;
	}

	/**
	 * Enable or disable remote presence API calls
	 * 
	 * @param remoteServiceEnabled Set to true to enable remote presence requests, or false to disable
	 */
	public void setRemoteServiceEnabled(boolean remoteServiceEnabled) {
		this.remoteServiceEnabled = remoteServiceEnabled;
	}

	/**
	 * @return The RestOperations implementation being used to query the remote Openfire server
	 */
	public RestOperations getRestOperations() {
		return restOperations;
	}

	/**
	 * Sets the RestOperations implementation to be used to query the remote Openfire server
	 * 
	 * @param restOperations The RestOperations implementation to be used to query the remote Openfire server
	 */
	public void setRestOperations(RestOperations restOperations) {
		this.restOperations = restOperations;
	}

	/**
	 * @return The XPathOperations implementation being used to extract presence information from the REST responses
	 */
	public XPathOperations getXpathTemplate() {
		return xpathTemplate;
	}

	/**
	 * Sets the XPathOperations implementation to be used to extract presence information from the REST response
	 * 
	 * @param xpathTemplate The XPathOperations implementation to be used to extract presence information from the REST response
	 */
	public void setTemplate(XPathOperations xpathTemplate) {
		this.xpathTemplate = xpathTemplate;
	}

	/**
	 * Stores cached presence information for a single user and provides operations to determine its freshness
	 * 
	 * @author Paul Guare
	 *
	 */
	private class PresenceCacheEntry {
		
		/** The cached presence information **/
		private Presence presence;
		
		/** The time this cache entry was created **/
		private long timestamp;
		
		/**
		 * Creates a new cache entry for the specified presence information
		 * 
		 * @param presence The presence info to cache
		 */
		public PresenceCacheEntry(Presence presence) {
			this.presence = presence;
			timestamp = System.currentTimeMillis();
		}
		
		/**
		 * Has this cache entry expired?
		 * 
		 * @return true if this cache entry has expired, false otherwise
		 */
		public boolean isExpired() {
			return System.currentTimeMillis() > timestamp + cacheTimeout;
		}
		
		/**
		 * Returns the presence information stored in this cache entry
		 * 
		 * @return The cached presence information
		 */
		public Presence getPresence() {
			return presence;
		}	
	}

	@Override
	public Presence getUserPresence(String userName, boolean waitIfOffline) {
		return getUserPresence(userName);
	}
	
}
