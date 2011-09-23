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
import java.util.Arrays;

import me.normanmaurer.niosmtp.transport.impl.NettySMTPClientTransport;

import org.apache.james.protocols.api.handler.WiringException;
import org.apache.james.protocols.impl.NettyServer;
import org.apache.james.protocols.smtp.SMTPConfigurationImpl;
import org.apache.james.protocols.smtp.SMTPProtocol;
import org.apache.james.protocols.smtp.SMTPProtocolHandlerChain;
import org.apache.james.protocols.smtp.hook.SimpleHook;
import org.junit.Test;

public class SMTPProxyTest {


    protected static NettyServer create() throws WiringException {
        SMTPConfigurationImpl config = new SMTPConfigurationImpl();
        SMTPProtocolHandlerChain chain = new SMTPProtocolHandlerChain();
        chain.addHook(new SimpleHook());
        return new NettyServer(new SMTPProtocol(chain, config));
        
    }

    @Test
    public void test() throws Exception {
        NettyServer server = null;
        NettyServer proxy = null;
        NettySMTPClientTransport clientTransport = null;
        try {

            server = create();
            server.setListenAddresses(Arrays.asList(new InetSocketAddress(1025)));
            server.bind();

            clientTransport = NettySMTPClientTransport.createPlain();

            SMTPProxyProtocolHandlerChain chain = new SMTPProxyProtocolHandlerChain(clientTransport, new InetSocketAddress(1025));
            SMTPConfigurationImpl config = new SMTPConfigurationImpl();
            proxy = new NettyServer(new SMTPProtocol(chain, config));
            proxy.setListenAddresses(Arrays.asList(new InetSocketAddress(10025)));
            proxy.bind();
            
            
            
        } finally {
            if (proxy != null) {
                proxy.unbind();
            }
            if (clientTransport != null) {
                clientTransport.destroy();
            }
            
            if (server != null) {
                server.unbind();
            }
        }

    }
    
    public static void main(String args[]) throws Exception {
        if (args.length != 3) {
            throw new IllegalArgumentException("$proxyport $remotesmtpaddress $remotesmtpport");
        }
        NettyServer server = null;
        NettyServer proxy = null;
        NettySMTPClientTransport clientTransport = null;
        server = create();
        server.setListenAddresses(Arrays.asList(new InetSocketAddress(1025)));
        server.bind();

        clientTransport = NettySMTPClientTransport.createPlain();

        SMTPProxyProtocolHandlerChain chain = new SMTPProxyProtocolHandlerChain(clientTransport, new InetSocketAddress(args[1], Integer.parseInt(args[2])));
        SMTPConfigurationImpl config = new SMTPConfigurationImpl();
        proxy = new NettyServer(new SMTPProtocol(chain, config));
        proxy.setListenAddresses(Arrays.asList(new InetSocketAddress(Integer.parseInt(args[0]))));
        proxy.bind();

    }
}
