package com.surevine.alfresco.presence.xmpp;

/**
 * Represents a runtime error in the XMPP presence implementation
 * 
 * @author jonnyheavey
 *
 */
public class XMPPExecutionException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public XMPPExecutionException(String message) {
		super(message);
	}

	public XMPPExecutionException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public XMPPExecutionException(Throwable cause) {
		super(cause);
	}
	
}
