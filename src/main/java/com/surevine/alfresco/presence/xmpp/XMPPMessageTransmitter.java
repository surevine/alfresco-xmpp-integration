package com.surevine.alfresco.presence.xmpp;

public interface XMPPMessageTransmitter {
	
	public void sendMessage(XMPPUser sender, XMPPUser target, String message);

}
