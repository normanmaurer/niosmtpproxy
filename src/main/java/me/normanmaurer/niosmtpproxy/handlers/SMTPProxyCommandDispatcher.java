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

import java.util.Collections;
import java.util.List;

import me.normanmaurer.niosmtpproxy.SMTPProxyConstants;

import org.apache.james.protocols.api.handler.AbstractCommandDispatcher;
import org.apache.james.protocols.api.handler.CommandHandler;
import org.apache.james.protocols.smtp.SMTPResponse;
import org.apache.james.protocols.smtp.SMTPRetCode;
import org.apache.james.protocols.smtp.SMTPSession;

/**
 * {@link AbstractCommandDispatcher} which forwards all <code>UNKNOWN</code> commands to the "proxied" server
 * 
 * @author Norman Maurer
 *
 */
public class SMTPProxyCommandDispatcher extends AbstractCommandDispatcher<SMTPSession> implements SMTPProxyConstants {

    private final static CommandHandler<SMTPSession> UNKNOWN_CMD_HANDLER = new SMTPProxyCmdHandler();

    /**
     * 
     */
    @Override
    public void onLine(SMTPSession session, byte[] line) {
        
        // don't handle messages till we are connected. This is kind of a workaround for the async nature but also a feature ;)
        // its called EARLYTALKER check which checks if the SMTP client tries to transmit commands before we send out the greeting.
        // This is not valid as stated in the SMTP RFC
        if (!session.getConnectionState().containsKey(SMTPProxyConstants.SMTP_CLIENT_SESSION_KEY)) {
            SMTPResponse response = new SMTPResponse(SMTPRetCode.SERVICE_NOT_AVAILABLE, "Only talk to me once I said welcome...");
            response.setEndSession(true);
            session.writeResponse(response);
        } else {
            super.onLine(session, line);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<String> getMandatoryCommands() {
        return Collections.EMPTY_LIST;
    }

    @Override
    protected String getUnknownCommandHandlerIdentifier() {
        return SMTPProxyCmdHandler.class.getName();
    }

    @Override
    protected CommandHandler<SMTPSession> getUnknownCommandHandler() {
        return UNKNOWN_CMD_HANDLER;
    }

}
