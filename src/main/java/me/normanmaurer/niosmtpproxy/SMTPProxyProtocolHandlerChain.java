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

import me.normanmaurer.niosmtp.transport.SMTPClientTransport;
import me.normanmaurer.niosmtp.transport.impl.SMTPClientConfigImpl;
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

import org.apache.james.protocols.api.handler.ProtocolHandler;
import org.apache.james.protocols.api.handler.WiringException;
import org.apache.james.protocols.smtp.SMTPProtocolHandlerChain;
import org.apache.james.protocols.smtp.hook.Hook;


/**
 * {@link SMTPProtocolHandlerChain} which adds all needed {@link ProtocolHandler} to build up a SMTP Proxy
 * 
 * @author Norman Maurer
 *
 */
public class SMTPProxyProtocolHandlerChain extends SMTPProtocolHandlerChain{

    
    public SMTPProxyProtocolHandlerChain(SMTPClientTransport transport, InetSocketAddress remote) throws WiringException {
    	this(transport, remote, new Hook[0]);
    }
    
    public SMTPProxyProtocolHandlerChain(SMTPClientTransport transport, InetSocketAddress remote, Hook... hooks) throws WiringException {
    	super(false);
    	add(new SMTPProxyCommandDispatcher());
        add(new SMTPProxyEhloCmdHandler());
        add(new SMTPProxyHeloCmdHandler());
        add(new SMTPProxyMailCmdHandler());
        add(new SMTPProxyRcptCmdHandler());
        add(new SMTPProxyDataCmdHandler());
        add(new SMTPProxyDataLineHandler());
        add(new SMTPProxyAcceptingMessageHook());
        add(new SMTPProxyQuitCmdHandler());
        add(new SMTPProxyConnectHandler(transport, remote, new SMTPClientConfigImpl()));
        add(new SMTPProxyDisconnectHandler());
        
        for (Hook hook: hooks) {
        	add(hook);
        }
        wireExtensibleHandlers();
    }
}
