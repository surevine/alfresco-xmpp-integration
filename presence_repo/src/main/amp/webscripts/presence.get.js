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
var users = argsM['users'];

if (!users || users==null || users=="" || users==[]) {
        users=[];
        users.push(person.properties.userName);
}

var presences = [];
for each (user in users) {
		try {
			logger.log("Getting script presence for "+user);
			var presence=xmpp.getPresence(user);
			presences.push(presence);
		}
		catch (e) {
			if (users.length==1) {
				throw e;
			}
			logger.warn("Could not get presence of "+user+" due to: "+e);
		}
}

if (presences.length==0) {
	throw "Could not retrieve the presence of any of the specified users";
}

model.presences=presences;
