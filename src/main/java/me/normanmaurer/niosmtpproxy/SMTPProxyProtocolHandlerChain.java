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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.normanmaurer.niosmtp.transport.SMTPClientTransport;
import me.normanmaurer.niosmtpproxy.handlers.SMTPProxyAcceptingMessageHook;
import me.normanmaurer.niosmtpproxy.handlers.SMTPProxyCommandDispatcher;
import me.normanmaurer.niosmtpproxy.handlers.SMTPProxyDataCmdHandler;
import me.normanmaurer.niosmtpproxy.handlers.SMTPProxyDataLineHandler;
import me.normanmaurer.niosmtpproxy.handlers.SMTPProxyDisconnectHandler;
import me.normanmaurer.niosmtpproxy.handlers.SMTPProxyEhloCmdHandler;
import me.normanmaurer.niosmtpproxy.handlers.SMTPProxyHeloCmdHandler;
import me.normanmaurer.niosmtpproxy.handlers.SMTPProxyMailCmdHandler;
import me.normanmaurer.niosmtpproxy.handlers.SMTPProxyQuitCmdHandler;
import me.normanmaurer.niosmtpproxy.handlers.SMTPProxyRcptCmdHandler;

import org.apache.james.protocols.api.handler.AbstractProtocolHandlerChain;
import org.apache.james.protocols.api.handler.ProtocolHandler;
import org.apache.james.protocols.api.handler.WiringException;


/**
 * {@link AbstractProtocolHandlerChain} which adds all needed {@link ProtocolHandler} to build up a SMTP Proxy
 * 
 * @author Norman Maurer
 *
 */
public class SMTPProxyProtocolHandlerChain extends AbstractProtocolHandlerChain{

    private final List<Object> handlers;
    
    public SMTPProxyProtocolHandlerChain(SMTPClientTransport transport, InetSocketAddress remote) throws WiringException {
        List<Object> hList = new ArrayList<Object>();
        hList.add(new SMTPProxyCommandDispatcher());
        hList.add(new SMTPProxyEhloCmdHandler());
        hList.add(new SMTPProxyHeloCmdHandler());
        hList.add(new SMTPProxyMailCmdHandler());
        hList.add(new SMTPProxyRcptCmdHandler());
        hList.add(new SMTPProxyDataCmdHandler());
        hList.add(new SMTPProxyDataLineHandler());
        hList.add(new SMTPProxyAcceptingMessageHook());
        hList.add(new SMTPProxyQuitCmdHandler());
        hList.add(new SMTPProxyConnectHandler(transport, remote));
        hList.add(new SMTPProxyDisconnectHandler());
        handlers = Collections.unmodifiableList(hList);
        wireExtensibleHandlers();
    }
    
    @Override
    protected List<Object> getHandlers() {
        return handlers;
    }

}
