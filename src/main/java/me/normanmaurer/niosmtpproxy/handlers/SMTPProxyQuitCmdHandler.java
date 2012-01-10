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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtpproxy.SMTPResponseAdapter;

import org.apache.james.protocols.api.Request;
import org.apache.james.protocols.api.future.FutureResponseImpl;
import org.apache.james.protocols.smtp.SMTPSession;


/**
 * {@link SMTPProxyCmdHandler} which does close the {@link SMTPSession} after processing the {@link org.apache.james.protocols.smtp.SMTPResponse}
 * 
 * @author Norman Maurer
 *
 */
public class SMTPProxyQuitCmdHandler extends SMTPProxyCmdHandler {

    private final static Collection<String> COMMANDS = Collections.unmodifiableCollection(Arrays.asList("QUIT"));


    @Override
    protected SMTPProxyFutureListener createListener(final FutureResponseImpl response, SMTPSession session, Request request, SMTPClientSession clientSession) {
        return new SMTPProxyFutureListener(response) {

            @Override
            public void onResponse(SMTPClientSession clientSession, SMTPResponse serverResponse) {
            	response.setResponse(new SMTPResponseAdapter(serverResponse, true));
            }
            
        };
    }


    @Override
    public Collection<String> getImplCommands() {
        return COMMANDS;
    }

}
