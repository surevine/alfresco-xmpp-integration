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
var user = person.properties.userName;

var headers = xmpp.getUnreadMessageHeaders(user);

var scriptMessageHeaders=[],
	scriptInviteHeaders=[],
	messageCount = 0;


logger.log(headers);
for (var i=0; i < headers.size(); i++) {
logger.log('Found a header: '+headers.get(i));

        var header=headers.get(i);
        var jid = header.getJid();
        
        // Detect whether message is group invite
        if((jid.contains(".hablar-emite-")) && (jid.contains(".securitylabel-"))) {
        	scriptInviteHeaders[jid]=jid;
        	continue;
        }
        else {
        	
            var thisUser = header.getJid().substring(0, header.getJid().indexOf("@")); //The user name is everything before the @ (which splits username and domain)
            logger.log('user '+thisUser);
            if (scriptMessageHeaders[thisUser]) {
                    scriptMessageHeaders[thisUser]++;
            }
            else {
                    scriptMessageHeaders[thisUser]=1;
            }
        	
        }
        
}

model.messages=[];
model.invites=[];

for (var user in scriptMessageHeaders) {
	var message={};
	var person = people.getPerson(user);
	message.userName=user;
	if (person!=null && person.properties!=null) {
	  message.displayName=(person.properties.firstName + " " + person.properties.lastName).replace(/^\s+|\s+$/g, "");
	} else {
	  message.displayName=message.userName;
	}
	message.count=scriptMessageHeaders[user];
	model.messages.push(message);
	messageCount+=message.count;
}

for (var inviteJid in scriptInviteHeaders) {
	var invite = {};
	// format group chat name
    var groupChatName = inviteJid.substring(0, inviteJid.indexOf("."));
    groupChatName = groupChatName.replace(/_/g," ");
    groupChatName = groupChatName.charAt(0).toUpperCase() + groupChatName.slice(1);
	invite.groupChatName = groupChatName;
	model.invites.push(invite);
}

model.count = messageCount + model.invites.length;
