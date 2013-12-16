package com.surevine.alfresco.presence.xmpp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

public class ChatBasedXMPPMessageTransmitterImpl extends XMPPService implements XMPPMessageTransmitter {

	private static final Log _logger = LogFactory.getLog(ChatBasedXMPPMessageTransmitterImpl.class);
	
	protected String _domain="10.66.2.95";
	
	public void setDomain(String domain) {
		_domain=domain;
	}
	
	@Override
	public void sendMessage(XMPPUser sender, XMPPUser target, String message) {
		
		if (message==null) {
			throw new XMPPExecutionException("The message to send between "+sender+" and "+target+" cannot be null");
		}
		
		if (sender==null) {
			throw new XMPPExecutionException("The sender of the message ["+message+"] to "+target+" cannot be null");
		}
		
		if (target==null) {
			throw new XMPPExecutionException("The recipient of the message ["+message+"] from "+sender+" cannot be null");
		}
		
		if (!userExists(sender.getUsername())) {
			throw new XMPPExecutionException("The sender "+sender+" of the message ["+message+"] to "+target+" must exist");
		}
		
		if (!userExists(target.getUsername())) {
			throw new XMPPExecutionException("The recipient "+target+" of the message ["+message+"] from "+sender+" must exist");

		}
		
		Connection connection = getConnection(sender);
		ChatManager chatManager = connection.getChatManager();
		Chat chat = chatManager.createChat(target.getUsername()+"@"+_domain, new MessageListener() {
		    public void processMessage(Chat chat, Message message) {
		        _logger.debug("Received message: " + message);
		    }});
		try {
			chat.sendMessage(message.trim());
		}
		catch (XMPPException e) {
			_logger.error("Obtained "+e+" while sending a message from "+sender.getUsername()+" to "+target.getUsername());
			throw new XMPPExecutionException("Could not send a message from "+sender.getUsername()+" to "+target.getUsername());
		}
	}

}
