package com.surevine.alfresco.presence.xmpp;

import java.util.ArrayList;
import java.util.Iterator;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.OfflineMessageHeader;
import org.jivesoftware.smackx.OfflineMessageManager;
import org.jivesoftware.smackx.packet.DiscoverItems.Item;

public class MockOfflineMessageManager extends OfflineMessageManager {

	public MockOfflineMessageManager(Connection connection) {
		super(connection);
	}
	
	@Override
	public int getMessageCount() {
		return 1;
	}
	
	@Override
	public Iterator<OfflineMessageHeader> getHeaders() {
		ArrayList<OfflineMessageHeader> headers = new ArrayList<OfflineMessageHeader>();
		OfflineMessageHeader header = new OfflineMessageHeader(new Item("Header")) {
			public String getJid() {
				return "test@123.456.789/1234";
			}
		};
		headers.add(header);	
		return headers.iterator();
	}

	@Override
	public Iterator<Message> getMessages() {
		ArrayList<Message> messages = new ArrayList<Message>();
		Message message = new Message();
		messages.add(message);	
		return messages.iterator();
	}	
	
}
