package me.normanmaurer.niosmtpproxy;

import java.net.InetSocketAddress;
import java.util.Arrays;

import me.normanmaurer.niosmtp.transport.SMTPClientTransport;
import me.normanmaurer.niosmtp.transport.impl.NettySMTPClientTransport;

import org.apache.james.protocols.api.handler.WiringException;
import org.apache.james.protocols.impl.NettyProtocolTransport;
import org.apache.james.protocols.impl.NettyServer;
import org.apache.james.protocols.smtp.SMTPConfigurationImpl;
import org.apache.james.protocols.smtp.SMTPProtocol;
import org.apache.james.protocols.smtp.SMTPProtocolHandlerChain;
import org.apache.james.protocols.smtp.hook.Hook;
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
        NettyServer server = null;
        NettyServer proxy = null;
        NettySMTPClientTransport clientTransport = null;
        server = create();
        server.setListenAddresses(Arrays.asList(new InetSocketAddress(1025)));
        server.bind();

        clientTransport = NettySMTPClientTransport.createPlain();

        SMTPProxyProtocolHandlerChain chain = new SMTPProxyProtocolHandlerChain(clientTransport, new InetSocketAddress("mail.medianet-world.de", 25));
        SMTPConfigurationImpl config = new SMTPConfigurationImpl();
        proxy = new NettyServer(new SMTPProtocol(chain, config));
        proxy.setListenAddresses(Arrays.asList(new InetSocketAddress(10025)));
        proxy.bind();

    }
}
