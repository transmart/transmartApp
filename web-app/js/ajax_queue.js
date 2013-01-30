/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
  

/**
 * Overwrites ext-adapter behavior for allowing queuing of AJAX requests.
 *
 */
Ext.lib.Ajax._queue = [];
Ext.lib.Ajax._activeRequests = 0;
Ext.lib.Ajax._concurrentRequests = Ext.isGecko ? 4 : 2;

switch (true) { 
    case Ext.isIE8: 
        _concurrentRequests = window.maxConnectionsPerServer; 
    break; 
    case Ext.isIE: 
        _concurrentRequests = 2; 
    break; 
    case Ext.isSafari: 
    case Ext.isChrome: 
    case Ext.isGecko3: 
        _concurrentRequests = 4; 
    break; 
} 


Ext.lib.Ajax.abort=function(o, callback, isTimeout)
{
    if (this.isCallInProgress(o)) {
        o.conn.abort();
        window.clearInterval(this.poll[o.tId]);
        delete this.poll[o.tId];
        if (isTimeout) {
            delete this.timeout[o.tId];
        }

        //aborted may be called with no callback-parameter, so no loadexception
        //or else would be generated in handleTransactionResponse.
     //   if (!callback) {
      //      Ext.ux.util.MessageBus.publish('ext.lib.ajax.abort', {
     //           requestObject : o
     //       });
      //  }

        this.handleTransactionResponse(o, callback, true);

        return true;
    }
    else {

        for (var i = 0, max_i = this._queue.length; i < max_i; i++) {
            if (this._queue[i].o.tId == o.tId) {
                this._queue.splice(i, 1);
                break;
            }
        }

        return false;
    }
};

Ext.lib.Ajax.asyncRequest = function(method, uri, callback, postData)
{


    var o = this.getConnectionObject();

    if (!o) {
        return null;
    }
    else {

        this._queue.push({
           o : o,
           method: method,
           uri: uri,
           callback: callback,
           postData : postData,
           headers: this.headers
        });

        this._processQueue();

        return o;
    }
};

Ext.lib.Ajax._processQueue = function()
{
    var to = this._queue[0];

    if (to && this._activeRequests < this._concurrentRequests) {
        to = this._queue.shift();
        this._activeRequests++;
        this._asyncRequest(to.o, to.method, to.uri, to.callback, to.postData,to.headers);
    }


};

Ext.lib.Ajax._asyncRequest = function(o, method, uri, callback, postData,headers)
{
    o.conn.open(method, uri, true);

    if (this.useDefaultXhrHeader) {
        if (!this.defaultHeaders['X-Requested-With']) {
            this.initHeader('X-Requested-With', this.defaultXhrHeader, true);
        }
    }
     //  this.useDefaultHeader =false;
    if(headers!=null){
    	this.headers = headers;
    	this.hasHeaders=true;
	}
    
    if(postData && this.useDefaultHeader &&(!this.hasHeaders||!this.headers["Content-Type"])){
    	this.initHeader('Content-Type', this.defaultPostHeader);
    }


     if (this.hasDefaultHeaders || this.hasHeaders) {
        this.setHeader(o);
    }

    this.handleReadyState(o, callback);
    o.conn.send(postData || null);
    
};

Ext.lib.Ajax.releaseObject = function(o)
{
    o.conn = null;
    o = null;

    this._activeRequests--;
    this._processQueue();
};

Ext.lib.Ajax.handleTransactionResponse = function(o, callback, isAbort) {

    if (!callback) {
        this.releaseObject(o);
        return;
    }

    var httpStatus, responseObject;

    try {
        if (o.conn.status !== undefined && o.conn.status != 0) {
            httpStatus = o.conn.status;
        } else {
            httpStatus = 13030;
        }
    } catch(e) {
        httpStatus = 13030;
    }

    if (httpStatus >= 200 && httpStatus < 300) {
        responseObject = this.createResponseObject(o, callback.argument);
        if (callback.success) {
            if (!callback.scope) {
                callback.success(responseObject);
            } else {
                callback.success.apply(callback.scope, [responseObject]);
            }
        }
    } else {
        switch (httpStatus) {
            case 12002:
            case 12029:
            case 12030:
            case 12031:
            case 12152:
            case 13030:
                responseObject = this.createExceptionObject(o.tId, callback.argument, (isAbort ? isAbort : false));
                if (callback.failure) {
                    if (!callback.scope) {
                        callback.failure(responseObject);
                    } else {
                        callback.failure.apply(callback.scope, [responseObject]);
                    }
                }
            break;
                
            default:
                responseObject = this.createResponseObject(o, callback.argument);
                if (callback.failure) {
                    if (!callback.scope) {
                        callback.failure(responseObject);
                    } else {
                        callback.failure.apply(callback.scope, [responseObject]);
                    }
                }
            break;
        }
    }

    this.releaseObject(o);
    responseObject = null;
};