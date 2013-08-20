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
