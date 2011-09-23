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

import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtpproxy.FutureSMTPResponse;
import me.normanmaurer.niosmtpproxy.SMTPProxyConstants;

import org.apache.james.protocols.smtp.SMTPResponse;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.core.RcptCmdHandler;
import org.apache.james.protocols.smtp.hook.RcptHook;
import org.apache.mailet.MailAddress;

/**
 * Proxy RCPT commands to the remote SMTP Server after all {@link RcptHook}'s were called and non rejected 
 * it.
 * 
 * @author Norman Maurer
 *
 */
public class SMTPProxyRcptCmdHandler extends RcptCmdHandler implements SMTPProxyConstants{

    @Override
    protected SMTPResponse doCoreCmd(final SMTPSession session, String command, String parameters) {
        SMTPResponse response =  super.doCoreCmd(session, command, parameters);
        int retCode = Integer.parseInt(response.getRetCode());
        
        if (retCode < 400) {
            FutureSMTPResponse futureResponse = new FutureSMTPResponse();
            
            SMTPClientSession clientSession = (SMTPClientSession) session.getConnectionState().get(SMTP_CLIENT_SESSION_KEY);
            final MailAddress rcpt = (MailAddress) session.getState().get(CURRENT_RECIPIENT);
            clientSession.send(SMTPRequestImpl.rcpt(rcpt.toString()), new ExtensibleSMTPProxyResponseCallback(session, futureResponse){

                @SuppressWarnings("unchecked")
                @Override
                protected void onFailure(SMTPSession session, SMTPClientSession clientSession) {
                    Collection<MailAddress> recpients = (Collection<MailAddress>) session.getState().get(SMTPSession.RCPT_LIST);
                    recpients.remove(rcpt);                    
                }

                
            });
            return futureResponse;
            
        } else {
            return response;
        }
    }

}
