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

import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtpproxy.SMTPProxyConstants;

import org.apache.james.protocols.api.handler.DisconnectHandler;
import org.apache.james.protocols.smtp.SMTPSession;

/**
 * {@link DisconnectHandler} which makes sure the {@link SMTPClientSession} get disconnected
 * 
 * @author Norman Maurer
 *
 */
public class SMTPProxyDisconnectHandler implements DisconnectHandler<SMTPSession>, SMTPProxyConstants{

    @Override
    public void onDisconnect(SMTPSession session) {
        SMTPClientSession clientSession = (SMTPClientSession) session.getConnectionState().get(SMTP_CLIENT_SESSION_KEY);
        clientSession.close();
    }

}
