package me.normanmaurer.niosmtpproxy;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.normanmaurer.niosmtp.transport.SMTPClientTransport;

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
