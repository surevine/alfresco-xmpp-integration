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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import com.surevine.alfresco.presence.xmpp.XMPPConfiguration;
import com.surevine.alfresco.presence.xmpp.XMPPConfigurationException;
import com.surevine.alfresco.presence.xmpp.XMPPConnectionProvider;
import com.surevine.alfresco.presence.xmpp.XMPPExecutionException;
import com.surevine.alfresco.presence.xmpp.XMPPUser;

/**
 * Implementation of XMPPConnectionProvider, that stores users connection in an expiring pool, one connection for each user; and re-uses
 * them accordingly.
 * @author jheavey simonw
 *
 */
public class XMPPConnectionProviderImpl implements XMPPConnectionProvider {

	private static final Log _logger = LogFactory.getLog(XMPPConnectionProviderImpl.class);
	
	/**
	 * Actual connections for each user.
	 */
	private Map<XMPPUser, Connection> connections = new HashMap<XMPPUser, Connection>();
	
	/**
	 * Indicates the last time a given connection was used.  We have code in purgeExpiredConnections to try to ensure these
	 * two maps are consistent
	 */
	private Map<XMPPUser, Date> connectionActivity = new HashMap<XMPPUser, Date>();
	
	/**
	 * How long to leave a connection with no activity before it is purged.  Note that purging a connection will unset any presence
	 * that has been set
	 */
	private long CONNECTION_EXPIRY_LIMIT = 60000; // milliseconds (1 minute)
	
	/**
	 * The resource that this connection provider will use.  The default is fine for non-clustered instances, but clustered instances must select
	 * a unique resource for each node in the cluster, usually via Spring
	 */
	private String resource="Smack-Space";
	
	public void setResource(String resource) {
		this.resource=resource;
	}
	
	private DefaultPresenceProvider _defaultPresenceProvider = new SimpleDefaultPresenceProvider();
	
	public void setDefaultPresenceProvider(DefaultPresenceProvider provider) {
		_defaultPresenceProvider=provider;
	}

	/**
	 * Basic constructor.  Automatically schedules the purging of connections for every two minutes
	 */
	public XMPPConnectionProviderImpl() {
		if (_logger.isDebugEnabled()) {
			_logger.debug("XMPPConnectionProviderImpl created");
		}
		if (_logger.isTraceEnabled()) {
			_logger.trace("",new RuntimeException("Created for logging purposes only.  The system is fine."));
		}
		schedulePurging();
	}
	
	/**
	 * Takes effect from after the next run.  So if this is current set to 100 seconds, there are 50 secons to go until purging would
	 * next be run, and you change the time to 10 seconds, then purging will execute 50, 60, 70, 80... seconds from now
	 * @param expiryInSeconds
	 */
	public void setConnectionExpiryLimitInSeconds(long expiryInSeconds) {
		CONNECTION_EXPIRY_LIMIT=expiryInSeconds*1000l;
	}
	
	@Override
	public void closeConnection(XMPPConfiguration config, XMPPUser user) {
		closeConnection(config, user, null);
	}
	
	@Override
	public void closeConnection(XMPPConfiguration config, XMPPUser user, Presence p) {
		if (_logger.isTraceEnabled()) {
			_logger.trace("Closing the connection for "+user.getUsername());
		}
		Connection connection = connections.remove(user);
		if (connection==null) {
			if (_logger.isDebugEnabled()) {
				_logger.debug("Attempted to close a connection for "+user+" but no connection was found");
			}
			return;
		}
		connectionActivity.remove(user);

		try {
			if (p!=null) {
				connection.disconnect(p);
			}
			else {
				connection.disconnect();
			}
		}
		catch (Exception e) {
			if (_logger.isDebugEnabled()) {
				_logger.debug("Attempted to disconnect from a connection for "+user+" but failed.  This is probably not an issue of operational concern, and should be treated as normal behaviour", e);
			}
		}
	}
	
	@Override
	public Connection getConnection(XMPPConfiguration config, XMPPUser user) {
		
		if (_logger.isTraceEnabled()) {
			_logger.trace("Getting a connection for "+user.getUsername());
		}
		
		Connection connection = connections.get(user);
		
		if (connection!=null && !connection.isConnected()) {
			
			if (_logger.isDebugEnabled()) {
				_logger.debug("Found disconnected connection for "+user.getUsername()+". Reconnecting...");
			}
			try {
				connection.connect();
			}
			catch (Exception e) {
				_logger.warn("Could not reconnect the connection for "+user.getUsername()+".  Getting a new connection instead", e);
				connection=null;
			}
		}
		
		if (connection==null) {
			if (_logger.isInfoEnabled()) {
				_logger.info("No connection available for "+user.getUsername()+" so getting a new one");
			}
		
			ConnectionConfiguration cConfig = new ConnectionConfiguration(config.getHost(), config.getPort());
			
			cConfig.setRosterLoadedAtLogin(true);
			cConfig.setSendPresence(false); //Log in silently
			connection = new XMPPConnection(cConfig);
			
			connections.put(user, connection);
		
			synchronized(connection) {
				try {
					connection.connect();
				} catch (XMPPException e) {
					closeConnection(config, user);
					_logger.error("Could not make a connection as "+user.getUsername()+" with "+config, e);
					throw new XMPPConfigurationException("Could not connect with "+config, e);
				}
				
				try {
					if (_logger.isTraceEnabled()) {
						_logger.trace("Packet reply timeout:" +SmackConfiguration.getPacketReplyTimeout());
						_logger.trace("Logging in as "+user.getUsername());
					}
					connection.login(user.getUsername(), user.getPassword(), resource);
				} catch (XMPPException e) {
					closeConnection(config, user);
					_logger.error("Could not login as "+user.getUsername(), e);
					throw new XMPPExecutionException("Could not login as "+user, e);
				}
				
				try {
					Presence presence = _defaultPresenceProvider.getDefaultPresence(user);
					connection.sendPacket(presence);
				} catch (Exception e) {
					closeConnection(config, user);
					_logger.error("Could not set default presence for "+user.getUsername(), e);
					throw new XMPPExecutionException("Could not set default presence for "+user, e);
				}
			}
			
			if (_logger.isDebugEnabled()) {
				_logger.debug("Connection for "+user.getUsername()+" created");
			}
		}
		
		
		// Update entry for last user update
		connectionActivity.put(user, new Date());
		
		return connection;
		
	}
	
	/**
	 * Remove connections that haven't been used in a while (default two minutes).
	 * Note that the semantics of "used" here simply refer to the connection having been retrieved with
	 * getConnection() - even if the caller then promptly throws the connection away, we still count it
	 * as "used"
	 */
	protected void purgeExpiredConnections() {
		if (_logger.isDebugEnabled()) {
			_logger.debug(String.format("Running purge task against %d connections. Items expire in %dms",
					connections.keySet().size(), CONNECTION_EXPIRY_LIMIT));
		}
		
		Iterator<Entry<XMPPUser, Date>> connectionActivityEntries = new ArrayList<Entry<XMPPUser, Date>>(connectionActivity.entrySet()).iterator();
		
		long currentTime = new Date().getTime();
		
		while(connectionActivityEntries.hasNext()) {
			Entry<XMPPUser, Date> connectionActivityEntry = connectionActivityEntries.next();
			XMPPUser user = connectionActivityEntry.getKey();
			if (user.getUsername().equals(XMPPConfiguration.getConfiguration().getSuperUser().getUsername())) {
				if (_logger.isTraceEnabled()) {
					_logger.trace("Ignoring superuser connection for: "+user.getUsername());
				}
				continue;
			}
			Date lastUpdated = connectionActivityEntry.getValue();
			if (_logger.isTraceEnabled()) {
				_logger.trace("  Inspecting "+user+" last updated on "+lastUpdated);
			}
			
			if((currentTime - lastUpdated.getTime()) > CONNECTION_EXPIRY_LIMIT) {
				if (_logger.isInfoEnabled()) {
					_logger.info("Connection for "+user.getUsername()+" is idle.  Disconnecting");
				}
				
				Connection connection = connections.get(user);				
				try {
					connection.disconnect();
				} catch (Exception e) {
					_logger.warn("Could not disconnect the idle connection for "+user.getUsername(), e);
				}
				
				// Remove connection from pool
				connections.remove(user);
				connectionActivity.remove(user);				
			}
		}
		
		//Now check for connections with no activity entry and set one if we find any
		//In theory, we should never end up here, but it's good to check
		Iterator<XMPPUser> users = connections.keySet().iterator();
		while (users.hasNext()) {
			XMPPUser user = users.next();
			if (!connectionActivity.containsKey(user)) {
				_logger.warn("Found a connection for "+user.getUsername()+" with no activity date stamp, so setting to today");
				connectionActivity.put(user,  new Date());
			}
		}
	}
	
	private class PurgingThread extends Thread {
		
		private long _millisBetweenPurges;
		
		public PurgingThread(long millisBetweenPurges) {
			_millisBetweenPurges=millisBetweenPurges;
			
			setDaemon(true); // Allow JVM exit.
		}
		
		public void run() {
			while (true) {
				try {
					purgeExpiredConnections();
				}
				catch (Exception e) {
					_logger.error("Could not purge expired connections:  ",e);
				}
				finally {
					try {
						Thread.sleep(_millisBetweenPurges);
					} catch (InterruptedException e) {
						_logger.warn("The PurgeExpiredConnections thread was interrupted.  Coming out of sleep", e);
					}

				}
			}
		}
	}
	
	//Ensure only one purging thread runs at any given time
	private static PurgingThread _purgingThread = null;
	
	/**
	 * Schedules the purgeExpiredConnections method using a simple Thread
	 */
	private synchronized void schedulePurging() {
		
		if (_purgingThread==null) {
			if (_logger.isDebugEnabled()) {
				_logger.debug("Next purge will occur in "+CONNECTION_EXPIRY_LIMIT+"ms");
			}
			_purgingThread = new PurgingThread(CONNECTION_EXPIRY_LIMIT);
			_purgingThread.start();
		}
		else {
			_logger.warn("The purging thread is already started");
		}
	}
		
}
