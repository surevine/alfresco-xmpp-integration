package com.surevine.alfresco.presence.xmpp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.packet.Presence;

public class SimplePresence {
	
	private static final Log _logger = LogFactory.getLog(SimplePresence.class);


	public enum Availability { AVAILABLE, BUSY, OFFLINE };
	
	private Availability _availability;
	
	private XMPPUser _user;
	
	private String _source;
	
	private String _message;
	
	protected SimplePresence() { }
	
	public SimplePresence(Availability availability, XMPPUser user, String source, String message) {
		_availability=availability;
		_user=user;
		_source=source;
		if (message==null) {
			_message="";
		}
		else {
			_message=message;
		}
	}
	
	public Availability getAvailability() {
		return _availability;
	}
	
	public String getUserName() {
		if (_user==null) {
			return "";
		}
		return _user.getUsername();
	}
	
	public String getSource() {
		return _source;
	}
	
	public String getMessage() {
		return _message;
	}
	
	public XMPPUser getUser() {
		return _user;
	}
	
	/**
	 * Returns a Smack version of this presence.  Note that some information, such as the hostname part of the JID, gets lost in translation
	 * @return
	 */
	public Presence convertToXMPPPresence() {
		if (_logger.isDebugEnabled()) {
			_logger.debug("Creating an XMPP Presence from "+this);
		}
		
		Availability workingAvailability = _availability;
		
		//Treat null as offline
		if (_availability==null) {
			workingAvailability=Availability.OFFLINE;
		}
		
		Presence.Type type=Presence.Type.available;
		if (workingAvailability.equals(Availability.OFFLINE)) {
			type=Presence.Type.unavailable;
		}
		Presence p = new Presence(type);
		switch (workingAvailability) {
			case AVAILABLE:
				p.setMode(Presence.Mode.available);
				break;
			case BUSY:
				p.setMode(Presence.Mode.dnd);
				break;
			case OFFLINE:
				p.setMode(Presence.Mode.xa);
		}
		if (_source.equals("chat")) {
			p.setFrom(getUserName()+"@unknown/emite");
			p.setPriority(0);
		}
		else if (_source.equals("space")){
			p.setFrom(getUserName()+"@unknown/space");
			p.setPriority(-1);
		}
		else {
			p.setFrom(getUserName()+"@unknown/Unknown");
			p.setPriority(0);
		}
		p.setStatus(_message);
		return p;
	}
	
	
	public static SimplePresence fromSmackPresence(Presence p, String queryUserName) {
		if (_logger.isDebugEnabled()) {
			_logger.debug("Creating a simplePresence from "+p);
		}
		String superUserPassword = XMPPConfiguration.getConfiguration().getSuperUser().getPassword();
		//Special handling for null presence
		if (p==null) {
			return new SimplePresence(Availability.OFFLINE, new XMPPUser(queryUserName, superUserPassword), "offline", "");
		}
		SimplePresence sp = new SimplePresence();
		sp._availability=extractAvailability(p);
		sp._source=extractSource(p);
		if (p.getStatus()==null) {
			sp._message="";
		}
		else {
			sp._message=p.getStatus();
		}
		String userName = extractUserName(p); //Get the user name
		sp._user=new XMPPUser(userName, superUserPassword);
		return sp;
	}
	
	protected static String extractSource(Presence p) {
		if (p!=null && p.getFrom()!=null && p.getFrom().contains("Smack-Space")) {
			return "space";
		}
		return "chat";
	}
	
	protected static String extractUserName(Presence p) {
		String resource = p.getFrom();
		if (resource==null) {
			return "unknown";
		}
		//else
		int trimIdx = resource.indexOf('@'); //Get everything before the @
		if (trimIdx==-1) {
			trimIdx = resource.indexOf('/'); //In the unlikely event there is no @, get everything before the /
		}
		if (trimIdx==-1) { //In the even more unlikely event there isn't a /, return the input String
			return resource;
		}
		return resource.substring(0, trimIdx);
	}
	
	/**
	 * Type = unavailable mode = whatever = offline
	 *  type = available mode=available|chat = available
	 *  type = available mode= not null, not available|chat = unavailable but online
	 *  type = available mode=null = available
	 */
	protected static Availability extractAvailability(Presence p) {		
		if (p.getType() == null || p.getType().equals(Presence.Type.unavailable)) {
			return Availability.OFFLINE;
		}
		
		Presence.Mode mode = p.getMode();
		if (mode!=null) {
			if (mode.equals(Presence.Mode.available) || mode.equals(Presence.Mode.chat) ) {
				return Availability.AVAILABLE;
			}
			else {
				return Availability.BUSY;
			}
		}
		//It looks like that logging into chat doesn't set the mode until you actually change your presence - so if the mode isn't
		//set, we infer a mode from the Type, which we know at this point is "available"
		else {
			return Availability.AVAILABLE;
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(100);
		sb.append("SimplePresence[ ");
		sb.append("Availability = ").append(_availability).append(" & ");
		sb.append("User = ").append(getUserName()).append(" & ");
		sb.append("Source = ").append(_source).append(" & ");
		sb.append("Message = ").append(_message).append(" ]");
		return sb.toString();
	}
	
	public boolean equals(Object o) {
		return this.toString().equals(o.toString());
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
	
}
