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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

/**
 * Implementation of XMPPPresenceService that performs operations as, essentially, a super-user,
 * on a long lived connection
 * @author jheavey simonw
 *
 */
public class XMPPPresenceServiceImpl extends XMPPService implements XMPPPresenceService, DefaultPresenceProvider {

	private static final Log _logger = LogFactory.getLog(XMPPPresenceServiceImpl.class);
	
	private Map<String, Integer> _ignoreDisconnects = new HashMap<String, Integer>();
	
	/**
	 * Map containing the services view of each users' presence
	 */
	protected Map<String, Map<String, PresenceEntry>> presences = new HashMap<String, Map<String, PresenceEntry>>();
	
	public class PresenceEntry {
		private Presence _presence;
		private Date _lastChecked;
		
		public Presence getPresence() { return _presence; }
		public Date getLastChecked() { return _lastChecked; }
		
		public PresenceEntry(Presence presence) {
			_presence=presence;
			_lastChecked=new Date();
		}
		
		public void setChecked() {
			_lastChecked=new Date();
		}
		
		public String toString() {
			return "PresenceEntry [Pri="+_presence.getPriority()+" Status="+_presence.getStatus()+" lastChecked="+_lastChecked+"]";
		}
		
		
	}
	
	/**
	 * The server part of JIDs managed by this presence service.  Default should usually work as == host
	 */
	private String xmppServer=XMPPConfiguration.getConfiguration().getHost();
	
	public XMPPPresenceServiceImpl() {
		if (_logger.isDebugEnabled()) {
			_logger.debug("XMPPPresenceServiceImpl created");
		}
		if (_logger.isTraceEnabled()) {
			_logger.trace(new RuntimeException("Created for logging purposes only.  The system is fine."));
		}
	}
	
	public void setXMPPServer(String s) {
		xmppServer=s;
	}
	
	/**
	 * We only want to set-up the roster once (although see forgetSuperUserRoster, below).  We don't want to do it in the constructor because
	 * we don't want exceptions percolating into spring and preventing the application context from being loaded
	 */
	protected boolean isRosterSetup=false;
	
	private Roster superUserRoster;
	
	/**
	 * Upon use, test the roster if it hasn't been used in this many seconds.
	 * The special value -1 disables testing
	 */
	private int rosterTestFrequency=60;
	public void setRosterTestFrequency(int fq) {
		rosterTestFrequency=fq;
	}
	
	/**
	 * How often should the fidelity of the super-user connection be monitored,
	 * in seconds.  If the super user conneciton is found to be broken in some way,
	 * then it will be discarded and a new one created.
	 */
	private int superUserConnectionMonitorFrequency=60;
	public void setSuperUserConnectionMonitorFrequency(int fq) {
		superUserConnectionMonitorFrequency=fq;
	}
	
	/**
	 * Test the roster by looking for this username, which must always exist
	 */
	private String testUserName="admin";
	public void setTestUserName(String userName) {
		testUserName=userName;
	}
	
	public synchronized void ignoreNextDisconnectFor(XMPPUser user) {
		
		if (_logger.isTraceEnabled()) {
			_logger.trace("Ignoring next disconnection for "+user.getUsername());
		}
		Integer currentValue = _ignoreDisconnects.get(user.getUsername());
		if (currentValue==null) {
			_ignoreDisconnects.put(user.getUsername(), 1);
		}
		else {
			_ignoreDisconnects.put(user.getUsername(), currentValue+1);
		}
	}
	
	private Date nextRosterTestDate = new Date(0l);
	
	/**
	 * Because the super-user roster maintains a connection to the backend database, we will need to forget about that 
	 * roster if the connection backing the roster is closed.  See XMPPConnectionProviderImpl
	 */
	public void forgetSuperUserRoster() {
		if (_logger.isTraceEnabled()) {
			_logger.trace("Forgetting roster");
		}
		isRosterSetup=false;
	}
	
	protected synchronized void setUpRoster() {
		//If we've already set the roster up, do nothing
		if (isRosterSetup) {
			if (_logger.isTraceEnabled()) {
				_logger.trace("Roster is already setup");
			}
			if (rosterTestFrequency!=-1 && new Date().after(nextRosterTestDate)) {
				nextRosterTestDate = new Date(new Date().getTime()+(1000l*rosterTestFrequency));
				if (_logger.isDebugEnabled()) {
					_logger.debug("Testing roster");
				}
				try {
					getPresence(testUserName);
				}
				catch (Exception e) {
					_logger.warn("Roster presence testing failed", e);
					resetConnection(XMPPConfiguration.getConfiguration().getSuperUser());
				}
			}
			return;
		}
		
		if (_logger.isTraceEnabled()) {
			_logger.trace("Setting up roster");
		}
		
		XMPPUser superuser = XMPPConfiguration.getConfiguration().getSuperUser();
		
		Connection connection = null;
		try {
			connection = getConnection(superuser);
		}
		catch(Exception e) {
			_logger.error("Error getting connection for superuser: "+superuser.getUsername(), e);
			throw new XMPPExecutionException("Error getting connection for superuser: "+superuser.getUsername(), e);
		}
		
		Presence presence = new Presence(Presence.Type.available,this.getClass().getSimpleName()+" connection", 1, Presence.Mode.available);
		SuperUserConnectionMonitor monitorThread = new SuperUserConnectionMonitor(connection, superUserConnectionMonitorFrequency, presence);
		monitorThread.setDaemon(true);
		monitorThread.start();
		
		if (_logger.isInfoEnabled()) {
			_logger.info("Retrieving roster");
		}
		
		superUserRoster = connection.getRoster();
		superUserRoster.addRosterListener(new RosterListener() {
			@Override
			public void presenceChanged(Presence newPresence) {
				if (_logger.isTraceEnabled()) {
					_logger.trace("Presence change packet recevied for user "+newPresence.getFrom()+". Recevied presence is "+newPresence.getType()+"|"+newPresence.getMode()+"|"+newPresence.getPriority());
				}
				
				// Retrieve existing presence for user
				final String shortJid = newPresence.getFrom().substring(0, newPresence.getFrom().indexOf('/'));
				
				Map<String, PresenceEntry> entries = presences.get(shortJid);
				if (entries == null) {
					entries = new HashMap<String, PresenceEntry>();
				}
				
				if (_logger.isTraceEnabled()) {
					_logger.trace(String.format("Retrieved %d entries for user %s", entries.size(), shortJid));
				}
				
				// Check whether received packet indicates user has gone offline
				if (newPresence.getType().equals(Presence.Type.unavailable)) {
					if (entries.keySet().contains(newPresence.getFrom())) {

						//Decide whether or not to ignore the disconnection presence
						boolean ignoreDisconnect=false;
						String bareUserName=shortJid.substring(0, shortJid.indexOf('@'));
						if (_ignoreDisconnects.get(shortJid)!=null) {
							int numberOfDisconnectsToIgnore = _ignoreDisconnects.get(bareUserName);
							if (numberOfDisconnectsToIgnore>0) {
								_ignoreDisconnects.put(bareUserName, numberOfDisconnectsToIgnore--);
								ignoreDisconnect=true;
								_logger.trace("Ignoring disconnection presence for "+shortJid+" "+(numberOfDisconnectsToIgnore-1)+" further disconnects will be ignored for this user");
							}
						}
						
						if (!ignoreDisconnect) {
							if (_logger.isTraceEnabled()) {
								_logger.trace(String.format("Removing entry for %s with jid %s.", shortJid, newPresence.getFrom()));
							}
						
							entries.remove(newPresence.getFrom());
						}
					}
				} else {
					if (_logger.isTraceEnabled()) {
						_logger.trace(String.format("Adding entry for %s with jid %s.", shortJid, newPresence.getFrom()));
					}
					
					entries.put(newPresence.getFrom(), new PresenceEntry(newPresence));
				}
				
				if (entries.isEmpty()) {
					presences.remove(shortJid);
				} else {
					presences.put(shortJid, entries);
				}
			}
			
			/**
			 * Intentionally do nothing
			 */
			@Override
			public void entriesUpdated(Collection<String> addresses) {				
			}
			
			/**
			 * Intentionally do nothing
			 */
			@Override
			public void entriesDeleted(Collection<String> addresses) {				
			}
			
			/**
			 * Intentionally do nothing
			 */
			@Override
			public void entriesAdded(Collection<String> addresses) {				
			}
		});
		
		isRosterSetup=true;
	}
	
	@Override
	public SimplePresence getPresence(String username) {
		return getPresence(username, true);
	}
	
	public SimplePresence getPresence(String username, boolean waitIfOffline) {
		try {
			return SimplePresence.fromSmackPresence(getPresenceRetryExpiredPresences(username, waitIfOffline), username);
		} catch (XMPPExecutionException e) {
			if (e.getCause()!=null && e.getCause() instanceof IllegalStateException) {
				// This means that the underlying connection behind the roster is bust, probably because the connection has been
				// timed out.  to fix this, we can throw the roster away and build a new one from a new connection
				if (_logger.isDebugEnabled()) {
					_logger.debug("Rebuilding roster due to IllegalStateException in getPresence");
					if (_logger.isTraceEnabled()) {
						_logger.trace("Underlying Exception was: ", e.getCause());
					}
				}
				forgetSuperUserRoster();
				//Note that if this still throws an IllegalStateException, it means something else has gone wrong and this exception gets handled as normal
				return SimplePresence.fromSmackPresence(getPresenceRetryExpiredPresences(username, waitIfOffline), username);
			} else { //If we've failed with an underlying cause that's not an IllegalStateException, then simply re-throw the error
				if (_logger.isTraceEnabled()) {
					_logger.trace("Rethrowing "+e);
				}
				throw e;
			}
		}
	}
	
	protected Presence getPresenceRetryExpiredPresences(String username, boolean waitIfOffline) {
		PresenceEntry pe = getPresenceImpl(username, waitIfOffline); 
		
		//If it's expired, forget about the previous entry and try again
		if (_logger.isTraceEnabled() && pe!=null) {
			_logger.trace("Last checked:  "+pe.getLastChecked());
		}
		
		//This expiration code wasn't working as intended so I've removed it - but we do probably want some logic to detect
		//if the user is disconnected from openfire here and then set their presence accordingly
		if (pe==null) {
			return null;
		}
		return pe.getPresence();
	}
	
	protected PresenceEntry getPresenceImpl(String username, boolean waitIfOffline) {
		setUpRoster();
		
		if (username==null || username.trim().equals("")) {
			_logger.error("Username cannot be null or the empty string, and was: "+username);
			throw new XMPPExecutionException(new IllegalArgumentException("Username cannot be null or the empty string, and was: "+username));
		}

		final String shortJid = username +"@" +xmppServer;
		
		// Add user to superuser's roster if not already entered
		if(!superUserRoster.contains(shortJid)) {
			if (_logger.isDebugEnabled()) {
				_logger.debug(username+" was not already in my roster, so adding them");
			}
			if(!userExists(username)) {
				_logger.error("User does not exist: "+username);
				throw new XMPPExecutionException("User does not exist: "+username);
			}

			try {
				superUserRoster.createEntry(shortJid, username, null);
			} catch (XMPPException e) {
				_logger.error("Error attempting to create roster entry for: "+username, e);
				throw new XMPPExecutionException("Error attempting to create roster entry for: "+username, e);
			}
			catch (IllegalStateException e) {
				_logger.error("Error attempting to create roster entry for: "+username, e);
				throw new XMPPExecutionException("Error attempting to create roster entry for: "+username, e);
			}
		}
		
		if (_logger.isDebugEnabled()) {
			_logger.debug("Retrieving last known presence for " +username);
		}
		
		// Get all the presence sessions for this user.
		final Map<String, PresenceEntry> presenceEntries = presences.get(shortJid);
		
		if (presenceEntries == null) {
			if (_logger.isInfoEnabled()) {
				_logger.info("No known presence for " +username);
			}
			
			return null;
		}
		
		if (_logger.isTraceEnabled()) {
			_logger.trace(String.format("Retrieved %d entries for user %s", presenceEntries.size(), shortJid));
		}
		
		PresenceEntry knownPresence = presencePicker(presenceEntries.values());
		int retries = 0;
		while (waitIfOffline && presenceEntries.isEmpty() && retries++ < 3) {
			if (_logger.isTraceEnabled()) {
				_logger.trace("Presence unknown for " +username +".  Waiting 100ms then retrying (attempt "+retries+" of 3)");
			}
			try {
				Thread.sleep(100l);
			} catch (InterruptedException e) {
				break;
			}
			
			knownPresence = presencePicker(presenceEntries.values());
		}
		
		return knownPresence;
	}
	
	/**
	 * Takes a set of presences from multiple sessions and derives one result.
	 */
	protected PresenceEntry presencePicker(final Collection<PresenceEntry> entries) {
		PresenceEntry result = null;
		
		final Iterator<PresenceEntry> it = entries.iterator();
		
		//Use the presence if...
		while (it.hasNext()) {

			final PresenceEntry entry = it.next();

			if (_logger.isDebugEnabled()) {
				_logger.debug("Comparing "+result+" with "+entry);
			}
			
			//... we don't have a result yet...
			if (result == null || (result.getPresence().getStatus()!=null && result.getPresence().getStatus().equals(Presence.Type.unavailable))) {
				if (_logger.isTraceEnabled()) {
					_logger.trace("Using this presence as we didn't already have one");
				}
				result = entry;
			}
			//... or this presence is higher priority than the last...
			else if (entry.getPresence().getPriority()>result.getPresence().getPriority()) {
				if (_logger.isTraceEnabled()) {
					_logger.trace("Using this presence as it has a higher priority than the previous presence");
				}
				result=entry;
			}
			//..or, if the priority is the same, this presence is newer than the last
			else if (entry.getPresence().getPriority()==result.getPresence().getPriority() && entry.getLastChecked().after(result.getLastChecked())) {
				result=entry;
				if (_logger.isTraceEnabled()) {
					_logger.trace("Using this presence as it has the same priority, but a latter date, than a previous presence");
				}
			}
		}
		
		return result;
	}
	
	@Override
	public void setPresence(SimplePresence sp) {
		setUpRoster();
		
		Presence presence = sp.convertToXMPPPresence();
		Connection connection = null;
		try {
			connection = getConnection(sp.getUser());
			if (_logger.isTraceEnabled()) {
				_logger.trace("Sending presence packet for "+sp);
			}
			connection.sendPacket(presence);
			if (_logger.isTraceEnabled()) {
				_logger.trace("  Packet sent");
			}
		} catch(Exception e) {
			_logger.error("Error setting presence: "+sp);
			if (hasCause(e, XMPPException.class) || hasCause(e, IllegalStateException.class)) {
				resetConnection(sp.getUser());
			}
			throw new XMPPExecutionException("Error setting presence: "+sp, e);
		}
	}
	
	protected boolean hasCause(Throwable t, Class<?> c) {
		if (!Throwable.class.isInstance(c)) {
			return false;
		}
		Throwable top = t;
		if (c.isInstance(top)) {
			return true;
		}
		
		while (top.getCause()!=null) {
			top=top.getCause();
			if (c.isInstance(top)) {
				return true;
			}
		}
		return false;
	}
	
	protected void resetConnection(XMPPUser user) {
		if (_logger.isDebugEnabled()) {
			_logger.debug("Resetting connection for "+user.getUsername());
		}
		
		try {
			/*
			 * Don't worry about not maintaining presence for the superuser as:
			 * 	A) No-one ever checks the super-user's presence
			 *  B) We can't check on his presence, as it's his connection we need to do so, and the whole reason we're in this method
			 *     is that we can't trust it anymore
			 */
			if (user.equals(XMPPConfiguration.getConfiguration().getSuperUser())) {
				getConnectionProvider().closeConnection(XMPPConfiguration.getConfiguration(), user);
				forgetSuperUserRoster();
			}
			else {
				Presence toSend = getPresence(user.getUsername()).convertToXMPPPresence();
				getConnectionProvider().closeConnection(XMPPConfiguration.getConfiguration(), user, toSend);
			}
		}
		catch (Exception e) {
			_logger.warn("Could not close the connection for "+user.getUsername()+" in order to reset it.  Will establish a new connection instead", e);
		}
		
		//Next, establish a new connection as that user
		getConnection(user);
		if (_logger.isTraceEnabled()) {
			_logger.trace("Connection reset for "+user.getUsername());
		}
	}

	@Override
	public Presence getDefaultPresence(XMPPUser user) {
		if (user.equals(XMPPConfiguration.getConfiguration().getSuperUser())) {
			return new Presence(Presence.Type.available, "", -2, Presence.Mode.available);
		}
		PresenceEntry pe = getPresenceImpl(user.getUsername(), true);
		if (pe==null) {
			return new Presence(Presence.Type.available, "", -2, Presence.Mode.available);
		}
		else {
			Presence p = pe.getPresence();
			return new Presence(p.getType(), p.getStatus(), p.getPriority(), p.getMode());
		}
	}

	/**
	 * Monitors the health of the super user connection.  If the connection appears unhealthy,
	 * this thread firstly attempts to fix the problem, and if it can't, throws away the super-user
	 * connection to allow another to be created.  This is done via forgetSuperUserRoster(), which
	 * will cause a new superuser connection to be created on-demand the next time one is required 
	 */
	private class SuperUserConnectionMonitor extends Thread {
		
		private Connection connection;
		private int frequency;
		private Presence presence;
		public SuperUserConnectionMonitor(Connection c, int f, Presence p) {
			connection=c; 
			frequency=f;
			presence=p;
		}
		
		public void run() {
			while (true) {
				if (connection.getUser()==null) {
					if (_logger.isDebugEnabled()) {
						_logger.debug("Conneciton has been closed.  Ending monitor thread");
					}
					forgetSuperUserRoster();
					break;
				}
				if (!connection.isConnected()) {
					_logger.warn("Connection for "+connection.getUser()+" is not connected.  Reconnecting...");
					try {
						connection.connect();
					} catch (XMPPException e) {
						_logger.error("Could not reconnect as "+connection.getUser(), e);
						forgetSuperUserRoster();
						break;
					}
				}
				if (!connection.isAuthenticated()) {
					_logger.warn("Connection for "+connection.getUser()+" is not authenticated.  Authenticating...");
					try {
						connection.login(connection.getUser(), "foo");
					} catch (XMPPException e) {
							_logger.error("Could not authenticate "+connection.getUser(), e);
							forgetSuperUserRoster();
							break;
					}
				}
				
				if (_logger.isTraceEnabled()) {
					_logger.trace("Sending presence packet");
				}
				try {
					connection.sendPacket(presence);
				}
				catch (Exception e) {
					_logger.error("Could not send presence packet for "+connection.getUser(), e);
					forgetSuperUserRoster();
					break;
				}
				
				if (_logger.isTraceEnabled()) {
					_logger.trace("Monitoring connection for "+connection.getUser()+" complete");
				}
				try {
					Thread.sleep(frequency*1000);
				} catch (InterruptedException e) {
					_logger.warn("Keealive thread for "+connection.getUser()+" interrupted");
					break;
				}
			}
		}
	}
}
