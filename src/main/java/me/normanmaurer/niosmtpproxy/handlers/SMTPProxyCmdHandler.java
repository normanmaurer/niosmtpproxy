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

import java.util.Collection;
import java.util.Collections;

import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtpproxy.FutureSMTPResponse;
import me.normanmaurer.niosmtpproxy.SMTPProxyConstants;

import org.apache.james.protocols.api.Request;
import org.apache.james.protocols.api.Response;
import org.apache.james.protocols.api.handler.CommandHandler;
import org.apache.james.protocols.smtp.SMTPSession;

/**
 * {@link CommandHandler} which just forwards all {@link Request} to an remote SMTP-Server. Once it get the {@link Response} of it, it will write
 * it back the SMTP Client. 
 * 
 * So this acts as a pure proxy
 * 
 * @author Norman Maurer
 *
 */
public class SMTPProxyCmdHandler implements CommandHandler<SMTPSession>, SMTPProxyConstants {

    @Override
    public Response onCommand(SMTPSession session, final Request request) {
        final FutureSMTPResponse futureResponse = new FutureSMTPResponse();
        
        SMTPClientSession clientSession = (SMTPClientSession) session.getConnectionState().get(SMTP_CLIENT_SESSION_KEY);
        clientSession.send(new SMTPRequestImpl(request.getCommand(), request.getArgument()), createCallback(futureResponse, session, request, clientSession)); 
        return futureResponse;
    }
    
    /**
     * Create a new {@link SMTPProxyResponseCallback}
     * 
     * @param response
     * @param session
     * @param request
     * @param clientSession
     * @return
     */
    protected SMTPResponseCallback createCallback(FutureSMTPResponse response, SMTPSession session, Request request, SMTPClientSession clientSession) {
        return new SMTPProxyResponseCallback(response);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> getImplCommands() {
        return Collections.EMPTY_LIST;
    }

}
