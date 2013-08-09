package com.surevine.alfresco.presence.xmpp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;

public class MockUserSearchManager extends UserSearchManager {
	
	public static final Collection<String> knownUsers;
	
	static {
		knownUsers = new ArrayList<String>();
		knownUsers.add("user0001-org01");
		knownUsers.add("admin");
		knownUsers.add("user0004-org02");
	}

	public MockUserSearchManager(Connection con) {
		super(con);
	}
	
	//User doesn't exist
	@Override
    public ReportedData getSearchResults(Form searchForm, String searchService) throws XMPPException {
		
		ReportedData output = new ReportedData();
		
		FormField usernameField = searchForm.getField("search");
		Iterator<String> usernames = usernameField.getValues();
		
		while(usernames.hasNext()) {
			
			String username = usernames.next();
			
			if(knownUsers.contains(username)) {
				
				// construct response for 'known' user
				Row row = new Row(null);
				output.addRow(row);
			}
			
		}
		
		return output;
	}	
	
	public Form getSearchForm(String s) {
		Form f = new Form("form");
		FormField userName = new FormField("Username");
		userName.setType("boolean");
		f.addField(userName);
		FormField search = new FormField("search");
		search.setType("jid-single");
		f.addField(search);
		return f;
	}
	

}
