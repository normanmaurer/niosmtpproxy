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

import org.apache.james.protocols.smtp.SMTPRetCode;

import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtpproxy.FutureSMTPResponse;

/**
 * {@link SMTPResponseCallback} which populate the {@link FutureSMTPResponse} and call {@link FutureSMTPResponse#markReady()}
 * 
 * @author Norman Maurer
 *
 */
public class SMTPProxyResponseCallback implements SMTPResponseCallback{

    private FutureSMTPResponse futureResponse;

    public SMTPProxyResponseCallback(FutureSMTPResponse futureResponse) {
        this.futureResponse = futureResponse;
    }
    
    @Override
    public void onResponse(SMTPClientSession clientSession, SMTPResponse serverResponse) {
        futureResponse.setRetCode(String.valueOf(serverResponse.getCode()));
        for (CharSequence seq: serverResponse.getLines()) {
            futureResponse.appendLine(seq);
        }
      
        futureResponse.markReady();
    }
    
    @Override
    public void onException(SMTPClientSession session, Throwable t) {
        futureResponse.setRetCode(SMTPRetCode.SERVICE_NOT_AVAILABLE);
        futureResponse.appendLine("Unable to handle request");
        futureResponse.setEndSession(true);
        futureResponse.markReady();
    }

}
