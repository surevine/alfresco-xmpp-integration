package com.surevine.alfresco.presence.xmpp;

/**
 * Exception class representing issue with XMPP Presence Configuration
 * 
 * @author jonnyheavey
 *
 */
public class XMPPConfigurationException extends RuntimeException {
	
	public XMPPConfigurationException(String message) {
		super(message);
	}
	
	public XMPPConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

}
