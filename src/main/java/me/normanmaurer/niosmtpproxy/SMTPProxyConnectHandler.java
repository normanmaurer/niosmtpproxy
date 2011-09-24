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
package me.normanmaurer.niosmtpproxy;

import java.net.InetSocketAddress;
import java.util.List;

import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.core.SMTPClientConfigImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;

import org.apache.james.protocols.api.handler.ConnectHandler;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.dsn.DSNStatus;

public class SMTPProxyConnectHandler implements ConnectHandler<SMTPSession>, SMTPProxyConstants{

    private final SMTPClientTransport transport;
    private final InetSocketAddress remote;

    public SMTPProxyConnectHandler(SMTPClientTransport transport, InetSocketAddress remote) {
        this.transport = transport;
        this.remote = remote;
    }
    
    
    @Override
    public org.apache.james.protocols.smtp.SMTPResponse onConnect(final SMTPSession session) {
        final FutureSMTPResponse futureResponse = new FutureSMTPResponse();
        
        transport.connect(remote, new SMTPClientConfigImpl(), new SMTPResponseCallback() {
            
            @Override
            public void onResponse(SMTPClientSession clientSession, SMTPResponse response) {
                session.getConnectionState().put(SMTP_CLIENT_SESSION_KEY, clientSession);
                futureResponse.setRetCode(String.valueOf(response.getCode()));
                List<String> lines = response.getLines();
                for (int i = 0; i < lines.size(); i++) {
                    futureResponse.appendLine(lines.get(i));
                }
                futureResponse.markReady();
            }
            
            @Override
            public void onException(SMTPClientSession clientSession, Throwable t) {
                t.getCause().printStackTrace();
                futureResponse.setRetCode(DSNStatus.getStatus(DSNStatus.TRANSIENT, DSNStatus.NETWORK_NO_ANSWER));
                futureResponse.appendLine("Unable to handle request");
                futureResponse.setEndSession(true);
                futureResponse.markReady();

            }
        });
        
        return futureResponse;
        
    }

}
