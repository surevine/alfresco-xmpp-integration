package com.surevine.alfresco.presence.xmpp;

import org.junit.BeforeClass;
import org.junit.Test;

import com.surevine.alfresco.presence.xmpp.ChatBasedXMPPMessageTransmitterImpl;

public class ChatBasedXMPPMessageTransmitterTest {

	protected static final ChatBasedXMPPMessageTransmitterImpl _fixture = new ChatBasedXMPPMessageTransmitterImpl() {
		protected boolean userExists(String userName) {
			return userName.equals("user0001-org01") || userName.equals("user0003-org01");
		}
	};
	
	@BeforeClass
	public static void setConnectionProvider() {
		_fixture.setXMPPConnectionProvider(new MockXMPPConnectionProvider());
	}
	
	@Test
	public void testSendMessageVanilla() {
		_fixture.sendMessage(new XMPPUser("user0001-org01", "validPassword"), new XMPPUser("user0003-org01", "validPassword"), "hello world");
	}
	
	@Test
	public void testSendMessageEmptyString() {
		_fixture.sendMessage(new XMPPUser("user0001-org01", "validPassword"), new XMPPUser("user0003-org01", "validPassword"), "");
	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testSendMessageNull() {
		_fixture.sendMessage(new XMPPUser("user0001-org01", "validPassword"), new XMPPUser("user0003-org01", "validPassword"), null);
	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testSendMessageNullFrom() {
		_fixture.sendMessage(null, new XMPPUser("user0003-org01", "validPassword"), "hello world");
	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testSendMessageNullTarget() {
		_fixture.sendMessage(new XMPPUser("user0001-org01", "validPassword"), null, "hello world");
	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testSendMessageSourceUserDoesntExist() {
		_fixture.sendMessage(new XMPPUser("idontexist-org01", "validPassword"), new XMPPUser("user0003-org01", "validPassword"), "hello world");
	}
	
	@Test(expected=XMPPExecutionException.class)
	public void testSendMessageTargetUserDoesntExist() {
		_fixture.sendMessage(new XMPPUser("user0001-org01", "validPassword"), new XMPPUser("idontexist-org01", "validPassword"), "hello world");
	}
}
