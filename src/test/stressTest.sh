#-------------------------------------------------------------------------------
# Copyright (C) 2008-2010 Surevine Limited.
#   
# Although intended for deployment and use alongside Alfresco this module should
# be considered 'Not a Contribution' as defined in Alfresco'sstandard contribution agreement, see
# http://www.alfresco.org/resource/AlfrescoContributionAgreementv2.pdf
# 
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
#-------------------------------------------------------------------------------
#!/bin/bash


#This is based on the dataload code and so some comments may refer to that rather than the presence stress tests

# Logging level definitions
DEBUG=1
INFO=2
WARN=3
ERROR=4

#Global parameters
LOAD_LOG_LEVEL=$WARN
TMP_DEST=/tmp/alfloader  
SESSION_LIFESPAN_MINUTES=30
HOSTNAME=10.66.2.187

################
#
#  Logs into Alfresco using CAS and stores the relevant cookies under
#  /tmp/alfloader/{userName} for other functions to reuse
#
#  Parameters:
#       1 - Alfresco Share host
#       2 - CAS host
#       3 - Username to pass to CAS
#       4 - Password for the above user.  Certs not yet supported, but could be if reqd
################
function createSession {
    DEST=http://$1/share/page/people-finder # This is a nice quick page to return
    CAS_HOSTNAME=$2
    USERNAME=$3
    PASSWORD=$4
    
    log "Creating a new session for $USERNAME" $INFO
    log "    Using password $PASSWORD" $DEBUG
        
    #Create temp directory if it doesn't already exist
    mkdir -p $TMP_DEST
    
    # URL-encode the destination (encoding method isn't perfect, may require upgrading in future)
    ENCODED_DEST=`echo $DEST | perl -p -e 's/([^A-Za-z0-9])/sprintf("%%%02X", ord($1))/seg' | sed 's/%2E/./g' | sed 's/%0A//g'`

    # Set up some temporary files to be used by cURL
    COOKIE_JAR=$TMP_DEST/.cookieJar.$3   # Name the cookie jar after the user to support multiple sessions
    HEADER_DUMP_DEST=$TMP_DEST/.headers.$$.$USERNAME  # Name headers file after the process to allow multiple runs at once
 
    log "Getting CAS ID" $DEBUG 
    # Visit CAS and get a login form. This includes a unique ID for the form, which we will 
    # store in CAS_ID and attach to our form submission. jsessionid cookie will be set here 
    CAS_ID=`curl -Ss -k -c $COOKIE_JAR https://$CAS_HOSTNAME/cas/login?service=$ENCODED_DEST | grep name=.lt | sed 's/.*value..//' | sed 's/\".*//'`

    # Submit the login form, using the cookies saved in the cookie jar and the form submission 
    # ID just extracted. We keep the headers from this request as the return value should 
    # be a 302 including a "ticket" param which we'll need in the next request
    log "Submitting login form" $DEBUG
    curl -sS -k --data "username=$USERNAME&password=$PASSWORD&lt=$CAS_ID&_eventId=submit&submit=Sign%20In" -i -b $COOKIE_JAR -c $COOKIE_JAR "https://$CAS_HOSTNAME/cas/login?service=$ENCODED_DEST" > $HEADER_DUMP_DEST 2>/dev/null

    # Linux may not need this line but my response from the previous call retrieves
    # windows-style linebreaks in OSX
    dos2unix $HEADER_DUMP_DEST > /dev/null 2>&1
    
    # Visit the URL with the ticket param to finally set the casprivacy and, more importantly, 
    # MOD_AUTH_CAS cookie. Now we've got a MOD_AUTH_CAS cookie, anything we do in this 
    # session will pass straight through CAS
    CURL_DEST=`cat < $HEADER_DUMP_DEST | grep Location | sed 's/Location: //'` 
    curl -Ss -k -b $COOKIE_JAR -c $COOKIE_JAR $CURL_DEST > /dev/null 2>&1
    
    # We now need to do a GET into alfresco before we can start doing any other requests,
    curl -Ss -k -b $COOKIE_JAR "$DEST" >/dev/null 2>/dev/null
    
    #We don't need the headers file anymore, so clean it up
    rm -f $HEADER_DUMP_DEST
    
    log "Session created for $USERNAME at $COOKIE_JAR" $DEBUG
}

################
#
#  Similar to createSession except, while createSession always creates a new session,
#  this method will look to see if an existing session already exists for the specified
#  user, and only create a new session if either no session exists, or the specified
#  session is more than (by default) 30 minutes old.  This saves us from having to work
#  around CAS ticket expiry issues as they arise by ensuring our sessions are well under
#  the various CAS timeouts
#  Parameters:
#       1 - Alfresco Share host
#       2 - CAS host
#       3 - Username to pass to CAS
#       4 - Password for the above user.  Certs not yet supported, but could be if reqd
function getSession {

    COOKIE_JAR=$TMP_DEST/.cookieJar.$3
    log "Attempting to reuse session from $COOKIE_JAR" $DEBUG
    if [ ! -f $COOKIE_JAR ]
    then
        log "$COOKIE_JAR does not exist.  Creating new session" $DEBUG
        createSession $1 $2 $3 $4
        return
    fi
    
    # Work out how old the cookie jar is
    NOW=`date +%s`
    COOKIE_JAR_CREATED=`stat -c %Z $COOKIE_JAR`
    AGE_SECONDS=`expr $NOW - $COOKIE_JAR_CREATED`
    AGE_MINUTES=`expr $AGE_SECONDS / 60`
    
    log "Session at $COOKIE_JAR is $AGE_MINUTES minutes old" $DEBUG
    
    if [ $AGE_MINUTES -gt $SESSION_LIFESPAN_MINUTES ]
    then
        log "$COOKIE_JAR has expired.  Creating new session" $DEBUG
        rm -f $COOKIE_JAR
        createSession $1 $2 $3 $4
        return
    fi
    
    # At this point, we know that a cookie jar for the given user exists and that it
    # has not expired, so we can simply exit the function
}

################
#
# Logs a message.  Log level is set with LOAD_LOG_LEVEL
#
# Parameters
#   1 - Message to log
#   2 - Level. See definitions at the start of this file
################
function log {
    MSG=$1
    LEVEL=$2
    
    if [ $LEVEL -ge $LOAD_LOG_LEVEL ]
    then
        echo $1
    fi
}

################
# Gets a users' presence
#
# Parameters
#   1 - The user name
#   2 - That users' password
#   3 - File to output results to
################
function getPresence {
    USER=$1
    PASSWORD=$2
    OUTPUT_FILE=$3
    ERROR_COLLECTOR=$4
    COOKIE_JAR=$TMP_DEST/.cookieJar.$USERNAME
    
    getSession $HOSTNAME $HOSTNAME $USER $PASSWORD
    log "Getting presence for $USER" $DEBUG
    RESULT=`curl -b $COOKIE_JAR "http://$HOSTNAME/share/proxy/alfresco/surevine/xmpp/presence" 2>/dev/null`
    echo "Presence of $USER : $RESULT" >> $OUTPUT_FILE
    echo $RESULT | grep presences.*status > /dev/null
    if [ $? -eq 1 ]
    then
     echo "getPresence $USER" >> $ERROR_COLLECTOR
    fi
}

################
# Gets a users' unread messages
#
# Parameters
#   1 - The user name
#   2 - That users' password
#   3 - File to output results to
################
function getUnreadMessages {

    USER=$1
    PASSWORD=$2
    OUTPUT_FILE=$3
    ERROR_COLLECTOR=$4
    COOKIE_JAR=$TMP_DEST/.cookieJar.$USERNAME
    
    getSession $HOSTNAME $HOSTNAME $USER $PASSWORD
    log "Getting unread messages for $USER" $DEBUG
    RESULT=`curl -b $COOKIE_JAR "http://$HOSTNAME/share/proxy/alfresco/surevine/xmpp/message-summary" 2>/dev/null`
    echo "Unread Messages of $USER : $RESULT" >> $OUTPUT_FILE
    echo $RESULT | grep count.*messages > /dev/null
    if [ $? -eq 1 ]
    then
      echo "getUnreadMessages $USER" >> $ERROR_COLLECTOR
    fi
}

################
# Runs a stress test
#
# Parameters
#   1 - The starting ID of users
#   2 - How many users to use
#   3 - How long to wait between requests
#   4 - How many times to loop
#   5 - The password (each user must have the same password right now)
function runStressTest {

    STARTING_USER=$1
    NUMBER_OF_USERS=$2
    WAIT_TIME=$3
    NUMBER_OF_LOOPS=$4
    PASSWORD=$5
    OUTPUT_FILE=$6
    STATUS_FILE=$7
    ERROR_COLLECTOR=$OUTPUT_FILE.errors

    cat < /dev/null > $OUTPUT_FILE
    cat < /dev/null > $ERROR_COLLECTOR

    echo "Establishing User Sessions"
    USER_COUNT=$STARTING_USER
    while [ $USER_COUNT -lt `expr $STARTING_USER + $NUMBER_OF_USERS` ]
    do
      USER=user$USER_COUNT-org01
      if [ `expr $USER_COUNT % 20` -ne 0 ]
      then 
         ( getSession $HOSTNAME $HOSTNAME $USER $PASSWORD; echo Session created for $USER ) &
      else
         getSession $HOSTNAME $HOSTNAME $USER $PASSWORD; echo Session created for $USER
      fi
      USER_COUNT=`expr $USER_COUNT + 1`
    done

    echo "Stress test is running.  Spooling $NUMBER_OF_USERS users over $WAIT_TIME seconds for $NUMBER_OF_LOOPS repetitions"
    echo "  Watch $STATUS_FILE for an ongoing report on active threads, $OUTPUT_FILE for raw results and $ERROR_COLLECTOR for details on which users encountered errors"

    RUNS=0
    while [ $RUNS -lt $NUMBER_OF_LOOPS ]
    do
    log "Starting a new run" $INFO
        USER_NUMBER=$STARTING_USER
        while [ $USER_NUMBER -lt `expr $STARTING_USER + $NUMBER_OF_USERS` ]
        do
        log "Running test for $USER_NUMBER" $INFO
            USER=user$USER_NUMBER-org01
            ( getPresence $USER $PASSWORD $OUTPUT_FILE $ERROR_COLLECTOR; sleep 1; getUnreadMessages $USER $PASSWORD $OUTPUT_FILE $ERROR_COLLECTOR ) &
            USER_NUMBER=`expr $USER_NUMBER + 1`
            sleep `bc <<< "scale=4; $WAIT_TIME/$NUMBER_OF_USERS"`
            statusReport $STATUS_FILE $OUTPUT_FILE $ERROR_COLLECTOR
        done
        RUNS=`expr $RUNS + 1`
    done

    while [ 1 ]
    do
       statusReport $STATUS_FILE $OUTPUT_FILE $ERROR_COLLECTOR
       sleep 1
    done
}

function statusReport {
    
    STATUS_FILE=$1
    OUTPUT_FILE=$2
    ERROR_COLLECTOR=$3

    PROCESSES=`ps | grep stressTest.sh | wc -l`
    PRESENCE_RESULTS=`cat < $OUTPUT_FILE | grep "Presence of" | wc -l`
    INBOX_RESULTS=`cat < $OUTPUT_FILE | grep "Unread Messages of" | wc -l`
    FAILURES=`cat < $ERROR_COLLECTOR | wc -l`
    CAS_REDIRECT=`cat < $OUTPUT_FILE | grep "title.*302.*title" | wc -l`
   
    echo "$PROCESSES Threads running" > $STATUS_FILE
    echo "$PRESENCE_RESULTS User Presences Returned" >> $STATUS_FILE
    echo "$INBOX_RESULTS Unread Message Results Returned" >> $STATUS_FILE
    echo "$FAILURES Failures Reported" >> $STATUS_FILE
    echo "   of which $CAS_REDIRECT were 302s from CAS" >> $STATUS_FILE
    echo "" >> $STATUS_FILE
    echo "When Only 2 processes are reported, the test is finished.  Use CTRL-C to exit" >> $STATUS_FILE
}

#rm -rf /tmp/alfloader
( runStressTest 1000 100 30 1 INSERT_PASSWORD_HERE results status) &
