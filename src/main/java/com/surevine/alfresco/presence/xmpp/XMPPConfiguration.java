package com.surevine.alfresco.presence.xmpp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.SmackConfiguration;

/**
 * Singleton configuration class containing details of an XMPP server to interact with.
 * 
 * @author jonnyheavey simonw
 *
 */
public class XMPPConfiguration {

	private static final Log _logger = LogFactory.getLog(XMPPConfiguration.class);

	
	private static XMPPConfiguration _instance=null;
	private static volatile Object _semaphore = new Object();
	
	private int xmppKeepAliveIntervalSeconds = 30;
	public void setXMPPKeepAliveIntervalSeconds(final int keepAlive) {
		xmppKeepAliveIntervalSeconds = keepAlive;
	}

	private int xmppTimeoutSeconds=30;
	public void setXMPPTimeoutSeconds(int to) {
		xmppTimeoutSeconds=to;
	}
	
	/**
	 * Public constructor to allow instantiation via Spring
	 */
	public XMPPConfiguration() {
		_instance=this;
		if (_logger.isDebugEnabled()) {
			_logger.debug("XMPPConfiguration created");
			if (_logger.isTraceEnabled()) {
				_logger.trace("Stack at XMPPConfiguration creation time: ", new RuntimeException("Thrown only for logging purposes"));
			}
		}
		
		SmackConfiguration.setPacketReplyTimeout(xmppTimeoutSeconds*1000);
		SmackConfiguration.setKeepAliveInterval(xmppKeepAliveIntervalSeconds*1000);
	}
		
	public static XMPPConfiguration getConfiguration() {
		
		//Yes, I know double-checked-locking has it's weaknesses, but with the volatile semaphore it's pretty much as good 
		//as you're going to get here
		if (_instance==null) {
			synchronized (_semaphore) {
				if (_instance==null) {
					_instance = new XMPPConfiguration();
				}
			}
		}
		return _instance;
	}

	private String host = "10.66.2.95";
	private int port = 5222;

	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		
		if(host==null || host.trim().equals("")) {
			throw new XMPPConfigurationException("host cannot be null or empty string. host was: "+host);
		}
		
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
	public String toString() {
		return "XMPPConfiguration[host="+host+" port="+port+"]";
	}
	
	protected String _superUserName = "superUser";
	public void setSuperUserName(String userName) {
		if(userName==null || userName.trim().equals("")) {
			throw new XMPPExecutionException("Username cannot be null or the empty string, and was: "+userName);
		}
		_superUserName=userName;
	}	
	
	private String _superUserPassword = "superUserPassword";
	public void setSuperUserPassword(String password) {
		if(password==null || password.trim().equals("")) {
			throw new XMPPExecutionException("Password cannot be null or the empty string, and was: "+password);
		}
		_superUserPassword=password;
	}
	
	/**
	 * Retrieve super user details
	 * @return the super user
	 */
	public XMPPUser getSuperUser() {
		return new XMPPUser(_superUserName, _superUserPassword);
	}
	
}
