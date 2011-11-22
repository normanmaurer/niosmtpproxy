/**
* Licensed to niosmtpproxy developers ('niosmtpproxy') under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* niosmtpproxy licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package me.normanmaurer.niosmtpproxy.handlers;

import org.apache.james.protocols.api.FutureResponseImpl;
import org.apache.james.protocols.smtp.SMTPSession;

import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * {@link SMTPProxyFutureListener} which will call {@link #onFailure(SMTPSession, SMTPClientSession)} once it 
 * either receive an {@link Exception} or if the received {@link SMTPResponse#getCode()} is >= 400.
 * 
 * @author Norman Maurer
 *
 */
public abstract class ExtensibleSMTPProxyFutureListener extends SMTPProxyFutureListener{

    private final SMTPSession session;


    public ExtensibleSMTPProxyFutureListener(SMTPSession session, FutureResponseImpl futureResponse) {
        super(futureResponse);
        this.session = session;
    }
    

    @Override
    public void onResponse(SMTPClientSession clientSession, SMTPResponse serverResponse) {
        if (serverResponse.getCode() >= 400) {
            onFailure(session, clientSession);
        }
        super.onResponse(clientSession, serverResponse);
    }

    @Override
    public void onException(SMTPClientSession clientSession, Throwable t) {
        onFailure(session, clientSession);
        super.onException(clientSession, t);
    }

    
    /**
     * Gets called on an {@link Exception} or when {@link SMTPResponse#getCode()} returns a PERM or TEMP error
     * 
     * @param session
     * @param clientSession
     */
    protected abstract void onFailure(SMTPSession session, SMTPClientSession clientSession);
}
