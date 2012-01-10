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
import java.util.List;

import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPClientFutureListener;
import me.normanmaurer.niosmtp.SMTPException;
import me.normanmaurer.niosmtp.core.SMTPMessageImpl;
import me.normanmaurer.niosmtp.core.SMTPMessageSubmitImpl;
import me.normanmaurer.niosmtp.transport.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtpproxy.SMTPProxyConstants;

import org.apache.james.protocols.api.Response;
import org.apache.james.protocols.api.future.FutureResponseImpl;
import org.apache.james.protocols.api.handler.WiringException;
import org.apache.james.protocols.smtp.MailEnvelopeImpl;
import org.apache.james.protocols.smtp.SMTPResponse;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.core.DataLineMessageHookHandler;
import org.apache.james.protocols.smtp.hook.MessageHook;

/**
 * SMTP Proxy which will forward the submitted message to the remote SMTP Server if no {@link MessageHook} did
 * reject it before. This will allow to filter on the message too. For this to work we will temporary store
 * the message on the proxy and cleanup it once we either forwarded it to the "real" SMTP Server or a {@link MessageHook}
 * rejected it.
 * 
 * @author Norman Maurer
 *
 */
public class SMTPProxyDataLineHandler extends DataLineMessageHookHandler implements SMTPProxyConstants{

    @Override
    protected Response processExtensions(SMTPSession session, MailEnvelopeImpl mail) {
        SMTPResponse response =  (SMTPResponse) super.processExtensions(session, mail);
        if (response == null || Integer.parseInt(response.getRetCode()) < 400) {
            FutureResponseImpl futureResponse = new FutureResponseImpl();
            SMTPClientSession clientSession = (SMTPClientSession) session.getConnectionState().get(SMTP_CLIENT_SESSION_KEY);
            final SMTPProxyFutureListener listener =  new SMTPProxyFutureListener(futureResponse);                    
            clientSession.send(new SMTPMessageSubmitImpl(new SMTPMessageImpl(mail.getMessageInputStream()), session.getRcptCount())).addListener(new SMTPClientFutureListener<FutureResult<Collection<me.normanmaurer.niosmtp.SMTPResponse>>>() {
                
                @Override
                public void operationComplete(SMTPClientFuture<FutureResult<Collection<me.normanmaurer.niosmtp.SMTPResponse>>> future) {
                    SMTPClientSession session = future.getSession();
                    FutureResult<Collection<me.normanmaurer.niosmtp.SMTPResponse>> result = future.getNoWait();
                    
                    SMTPException ex = null;
                    if (result.isSuccess()) {
                        Collection<me.normanmaurer.niosmtp.SMTPResponse> responses = result.getResult();
                        if (responses.size() == 1) {
                            listener.onResponse(session, responses.iterator().next()); 
                        } else {
                            ex = new SMTPException("Expected one SMTPResponse");
                        }
                    } else {
                        ex = result.getException();
                    }
                    if (ex != null) {
                        listener.onException(session, ex);
                    }
                }
            });
            return futureResponse;
        } else {
            return response;
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void checkMessageHookCount(List messageHandlers) throws WiringException {
        // Do nothing 
    }


}
