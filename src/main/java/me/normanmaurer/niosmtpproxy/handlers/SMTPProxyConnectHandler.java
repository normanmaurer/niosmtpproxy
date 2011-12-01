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

import java.net.InetSocketAddress;

import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPClientFutureListener;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.transport.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientConfig;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;
import me.normanmaurer.niosmtpproxy.SMTPProxyConstants;
import me.normanmaurer.niosmtpproxy.SMTPResponseAdapter;

import org.apache.james.protocols.api.FutureResponseImpl;
import org.apache.james.protocols.api.ProtocolSessionImpl;
import org.apache.james.protocols.api.ProtocolTransport;
import org.apache.james.protocols.api.Response;
import org.apache.james.protocols.api.handler.ConnectHandler;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.SMTPSessionImpl;
import org.apache.james.protocols.smtp.dsn.DSNStatus;

public class SMTPProxyConnectHandler implements ConnectHandler<SMTPSession>, SMTPProxyConstants{

    private final SMTPClientTransport transport;
    private final InetSocketAddress remote;
    private final SMTPClientConfig config;

    public SMTPProxyConnectHandler(SMTPClientTransport transport, InetSocketAddress remote, SMTPClientConfig config) {
        this.transport = transport;
        this.remote = remote;
        this.config = config;
    }
    
    
    @Override
    public Response onConnect(final SMTPSession session) {
        final FutureResponseImpl futureResponse = new FutureResponseImpl();
        
        // TODO: Remove this kind of hack
        final ProtocolTransport protocolTransport = ((ProtocolSessionImpl) session).getProtocolTransport();
        
        // suspend reads of the transport
        protocolTransport.setReadable(false);
        
        transport.connect(remote, config).addListener(createListener(session, futureResponse, protocolTransport));
        return futureResponse;
        
    }
    
    protected SMTPClientFutureListener<FutureResult<SMTPResponse>> createListener(final SMTPSession session, final FutureResponseImpl futureResponse, final ProtocolTransport protocolTransport) {
        return new SMTPClientFutureListener<FutureResult<SMTPResponse>>() {
            
            @Override
            public void operationComplete(SMTPClientFuture<FutureResult<SMTPResponse>> future) {
                SMTPClientSession session = future.getSession();
                FutureResult<SMTPResponse> result = future.getNoWait();
                if (result.isSuccess()) {
                    onResponse(session, result.getResult());
                } else {
                    onException(session, result.getException());
                }
            }
            
            private void onResponse(SMTPClientSession clientSession, SMTPResponse response) {
                session.getConnectionState().put(SMTP_CLIENT_SESSION_KEY, clientSession);
                clientSession.getCloseFuture().addListener(new SMTPClientFutureListener<FutureResult<FutureResult.Void>>() {

                    @Override
                    public void operationComplete(SMTPClientFuture<FutureResult<FutureResult.Void>> future) {
                        ((SMTPSessionImpl)session).getProtocolTransport().writeResponse(Response.DISCONNECT, session);                        
                    }
                });
                
                futureResponse.setResponse(new SMTPResponseAdapter(response, false));
                protocolTransport.setReadable(true);
               
            }
            
            private void onException(SMTPClientSession clientSession, Throwable t) {
                futureResponse.setResponse(new org.apache.james.protocols.smtp.SMTPResponse(DSNStatus.getStatus(DSNStatus.TRANSIENT, DSNStatus.NETWORK_NO_ANSWER), "Unable to handle request"));

            }
        };
    }

}
