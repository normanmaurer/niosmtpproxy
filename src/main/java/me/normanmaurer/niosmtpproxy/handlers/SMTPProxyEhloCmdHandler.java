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

import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.core.SMTPResponseImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientConstants;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtpproxy.SMTPProxyConstants;

import org.apache.james.protocols.api.Response;
import org.apache.james.protocols.api.ProtocolSession.State;
import org.apache.james.protocols.api.future.FutureResponseImpl;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.core.esmtp.EhloCmdHandler;
import org.apache.james.protocols.smtp.hook.HeloHook;

/**
 * Proxy the EHLO command to the remote SMTP Server after all {@link HeloHook} implementations were called
 * and non rejected it.
 * 
 * Beside this it also remove EXTENSIONS which was returned by the real SMTP Server but can not work in a proxy 
 * mode. This is explicit the STARTTLS EXTENSION.
 * 
 * STARTTLS will be implemented on the SMTP Proxy later.
 * 
 * @author Norman Maurer
 *
 */
public class SMTPProxyEhloCmdHandler extends EhloCmdHandler implements SMTPProxyConstants{

    @Override
    protected Response doCoreCmd(SMTPSession session, String command, String parameters) {
    	Response response =  super.doCoreCmd(session, command, parameters);
        int retCode = Integer.parseInt(response.getRetCode());
        
        // check if the return code was smaller then 400. If so we don't failed the command yet and so can forward it to the real server
        if (retCode < 400) {
            FutureResponseImpl futureResponse = new FutureResponseImpl();
            
            final SMTPClientSession clientSession = (SMTPClientSession) session.getAttachment(SMTP_CLIENT_SESSION_KEY, State.Connection);
            final String heloName = (String) session.getAttachment(SMTPSession.CURRENT_HELO_NAME, State.Transaction);
            clientSession.send(SMTPRequestImpl.ehlo(heloName)).addListener(new ExtensibleSMTPProxyFutureListener(session, futureResponse){

                @Override
                public void onResponse(SMTPClientSession clientSession, me.normanmaurer.niosmtp.SMTPResponse serverResponse) {
                    SMTPResponseImpl copiedResponse = new SMTPResponseImpl(serverResponse.getCode());
                    
                    boolean supportsAuth = false;
                    for (String extension: clientSession.getSupportedExtensions()) {
                        if (extension.startsWith(AUTH_EXTENSION_PREFIX)) {
                            supportsAuth = true;
                            break;
                        }
                    }
                    
                    for (String responseLine: serverResponse.getLines()) {
                        // remove announced STARTTLS
                        if (!responseLine.equalsIgnoreCase(SMTPClientConstants.STARTTLS_EXTENSION)) {
                            if (!supportsAuth && responseLine.startsWith(AUTH_EXTENSION_PREFIX)) {
                                // don't add the AUTH to extension if the remote server does not support it
                                //
                                // TODO: I'm not really happy with this impl atm. So needs review later
                                continue;
                            }
                            copiedResponse.addLine(responseLine);
                        }
                        
                    }
                    super.onResponse(clientSession, copiedResponse);
                }

                @Override
                protected void onFailure(SMTPSession session, SMTPClientSession clientSession) {
                    session.setAttachment(SMTPSession.CURRENT_HELO_NAME, null, State.Transaction);
                }
            });
            return futureResponse;
            
        } else {
            return response;
        }
    }

}
