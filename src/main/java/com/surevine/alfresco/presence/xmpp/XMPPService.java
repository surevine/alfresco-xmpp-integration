package com.surevine.alfresco.presence.xmpp;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;

/**
 * Provides the ability to connect to an XMPP server.
 * 
 * @author jonnyheavey
 *
 */
public abstract class XMPPService {

	private static final Log _logger = LogFactory.getLog(XMPPService.class);
	
	private XMPPConnectionProvider _xmppConnectionProvider;
	public void setXMPPConnectionProvider(XMPPConnectionProvider provider) {
		_xmppConnectionProvider=provider;
		if (this instanceof DefaultPresenceProvider) {
			_xmppConnectionProvider.setDefaultPresenceProvider((DefaultPresenceProvider)this);
		}
	}
	protected XMPPConnectionProvider getConnectionProvider() {
		return _xmppConnectionProvider;
	}
	
	private int _maxConnectionRetries=5;
	public void setMaxConnectionRetries(int retries) {
		_maxConnectionRetries=retries;
	}
	
	private long _retryWaitMillis=100l;
	public void setRetryWaitMillis(long retryWait) {
		_retryWaitMillis=retryWait;
	}
	
	protected abstract void resetConnection(XMPPUser user);

	
	/**
	 * Attempt to get a connection using the delegate impl method.  Retry a configurable number of times in the event of failure,
	 * waiting a configurable amount of time between each retry.  Defaults are to retry five times, waiting 1/10th of a second between
	 * each attempt.  This can be adjusted by setting the maxConnectionRetries and retryWaitMillis parameters using Spring
	 * @param user
	 * @return
	 */
	protected Connection getConnection(XMPPUser user) {
		for (int i=0; i < _maxConnectionRetries; i++) {
			try {
				return getConnectionImpl(user); //Try to get a connection, and if we get one, return
			}
			catch (XMPPExecutionException e) {
				if (i < (_maxConnectionRetries-1)) { //If we have retries left, just log the error at debug/trace and continue
					if (_logger.isDebugEnabled()) {
						_logger.debug("Couldn't get connection for "+user+".  Retrying (attempt "+i+" of "+_maxConnectionRetries+")");
						if (_logger.isTraceEnabled()) {
							_logger.trace("  Underlying exception: "+e, e);
						}
					}
				}
				else { //We've run out of retries, so throw the exception
					throw e;
				}
			}
			try {
				Thread.sleep(_retryWaitMillis); //Wait a given amount of time
			}
			catch (InterruptedException e) { } //If the wait is interrupted, just proceed with trying to get the next connection
		}
		//We should never get here, so throw a serious-sounding exception!
		throw new XMPPExecutionException("Unexpected path achieved in getConnection.  Please report this to support immediatley, as this should be impossible");
	}
	
	/**
	 * Establish an active connection with the XMPP server for a given user
	 * @param user to establish connection to XMPP server as
	 * @return connection
	 */
	protected Connection getConnectionImpl(XMPPUser user) {
		XMPPConfiguration config = XMPPConfiguration.getConfiguration();
		Connection connection = _xmppConnectionProvider.getConnection(config, user);
		return connection;
	}
	
	protected synchronized boolean userExists(String username) {
		
		XMPPUser superuser = XMPPConfiguration.getConfiguration().getSuperUser();
		
		Connection connection = null;
		try {
			connection = getConnection(superuser);
		}
		catch(Exception e) {
			_logger.error("Error getting connection for: "+superuser.getUsername(), e);
			throw new XMPPExecutionException("Error getting connection for: "+superuser.getUsername(), e);
		}

		UserSearchManager search = getUserSearchManager(connection);

        Form searchForm;
		try {
			searchForm = search.getSearchForm("search."+connection.getServiceName());
		} catch (XMPPException e) {
			resetConnection(superuser);
			_logger.error("Error getting search form for user lookup - user "+username, e);
			throw new XMPPExecutionException("Error getting search form for user lookup - user "+username, e);
		}

        Form answerForm = searchForm.createAnswerForm();  
        answerForm.setAnswer("Username", true);  
        answerForm.setAnswer("search", username);
		
        ReportedData data = null;
		try {
			data = search.getSearchResults(answerForm,"search."+connection.getServiceName());
		} catch (XMPPException e) {
			_logger.error("Error retrieving search results during user lookup - user "+username, e);
			throw new XMPPExecutionException("Error retrieving search results during user lookup - user "+username, e);
		}  

        if((data != null) && (data.getRows() != null))
        {
            Iterator<Row> it = data.getRows();
            while(it.hasNext())
            {
            	// user must exist on server
            	return true;
            }
        }
        
        // user doesn't exist
		return false;
	}
	
	/**
	 * Factory method for UserSearchManager, mainly exists to aid unit testing
	 */
	protected UserSearchManager getUserSearchManager(Connection c) {
		return new UserSearchManager(c);
	}
}
