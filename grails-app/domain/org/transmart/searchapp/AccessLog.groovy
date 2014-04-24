package org.transmart.searchapp
/**
 * Provides access to the logging table
 */

public class AccessLog {
    Long id
    String username;
    String event;
    String eventmessage;
    String requestURL;
    Date accesstime;

    static mapping = {
        table 'SEARCH_APP_ACCESS_LOG'
        id generator: 'sequence', params: [sequence: 'SEQ_SEARCH_DATA_ID']
        version false
        id column: 'id'
        username column: 'USER_NAME'
        event column: 'EVENT'
        eventmessage column: 'EVENT_MESSAGE'
        requestURL column: 'REQUEST_URL'
        accesstime column: 'ACCESS_TIME'
    }

    static constraints = {
        username(blank: false)
        event(nullable: false)
        eventmessage(nullable: true)
        requestURL(nullable: true)
        accesstime(nullable: false)
    }
}
