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
import me.normanmaurer.niosmtp.transport.netty.NettySMTPClientTransportFactory;

import org.apache.james.protocols.api.handler.WiringException;
import org.apache.james.protocols.api.logger.Logger;
import org.apache.james.protocols.netty.NettyServer;
import org.apache.james.protocols.smtp.SMTPConfigurationImpl;
import org.apache.james.protocols.smtp.SMTPProtocol;
import org.apache.james.protocols.smtp.SMTPProtocolHandlerChain;
import org.apache.james.protocols.smtp.hook.SimpleHook;
import org.junit.Test;

public class SMTPProxyTest {


    protected static NettyServer create() throws WiringException {
        SMTPConfigurationImpl config = new SMTPConfigurationImpl();
        SMTPProtocolHandlerChain chain = new SMTPProtocolHandlerChain();
        chain.add(new SimpleHook());
        return new NettyServer(new SMTPProtocol(chain, config, new SilentLogger()));
        
    }

    @Test
    public void test() throws Exception {
        NettyServer server = null;
        NettyServer proxy = null;
        SMTPClientTransport clientTransport = null;
        try {

            server = create();
            server.setListenAddresses(new InetSocketAddress(1025));
            server.bind();

            clientTransport = NettySMTPClientTransportFactory.createNio().createPlain();

            SMTPProxyProtocolHandlerChain chain = new SMTPProxyProtocolHandlerChain(clientTransport, new InetSocketAddress(1025));
            SMTPConfigurationImpl config = new SMTPConfigurationImpl();
            proxy = new NettyServer(new SMTPProtocol(chain, config, new SilentLogger()));
            proxy.setListenAddresses(new InetSocketAddress(10025));
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
    
    private final static class SilentLogger implements Logger {

        @Override
        public boolean isDebugEnabled() {
            return false;
        }

        @Override
        public boolean isErrorEnabled() {
            return false;
        }

        @Override
        public boolean isInfoEnabled() {
            return false;
        }

        @Override
        public boolean isTraceEnabled() {
            return false;
        }

        @Override
        public boolean isWarnEnabled() {
            return false;
        }

        @Override
        public void trace(String message) {
            
        }

        @Override
        public void trace(String message, Throwable t) {
            
        }

        @Override
        public void debug(String message) {
            
        }

        @Override
        public void debug(String message, Throwable t) {
            
        }

        @Override
        public void info(String message) {
            
        }

        @Override
        public void info(String message, Throwable t) {
            
        }

        @Override
        public void warn(String message) {
            
        }

        @Override
        public void warn(String message, Throwable t) {
            
        }

        @Override
        public void error(String message) {
            
        }

        @Override
        public void error(String message, Throwable t) {
            
        }
        
    }
    
    public static void main(String args[]) throws Exception {
        if (args.length != 3) {
            throw new IllegalArgumentException("$proxyport $remotesmtpaddress $remotesmtpport");
        }
        NettyServer proxy = null;
        SMTPClientTransport clientTransport = null;

        clientTransport = NettySMTPClientTransportFactory.createNio().createPlain();

        SMTPProxyProtocolHandlerChain chain = new SMTPProxyProtocolHandlerChain(clientTransport, new InetSocketAddress(args[1], Integer.parseInt(args[2])));
        SMTPConfigurationImpl config = new SMTPConfigurationImpl();
        proxy = new NettyServer(new SMTPProtocol(chain, config, new SilentLogger()));
        proxy.setListenAddresses(new InetSocketAddress(Integer.parseInt(args[0])));
        proxy.bind();

    }
}
