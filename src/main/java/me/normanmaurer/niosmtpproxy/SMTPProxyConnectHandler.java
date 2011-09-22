package me.normanmaurer.niosmtpproxy;

import java.net.InetSocketAddress;
import java.util.List;

import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.core.SMTPClientConfigImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;

import org.apache.james.protocols.api.handler.ConnectHandler;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.dsn.DSNStatus;

public class SMTPProxyConnectHandler implements ConnectHandler<SMTPSession>{

    private final SMTPClientTransport transport;
    private final InetSocketAddress remote;

    public SMTPProxyConnectHandler(SMTPClientTransport transport, InetSocketAddress remote) {
        this.transport = transport;
        this.remote = remote;
    }
    
    
    @Override
    public void onConnect(final SMTPSession session) {
        transport.connect(remote, new SMTPClientConfigImpl(), new SMTPResponseCallback() {
            
            @Override
            public void onResponse(SMTPClientSession clientSession, SMTPResponse response) {
                session.getState().put("SMTPCLIENTSESSION", clientSession);

                List<String> lines = response.getLines();
                org.apache.james.protocols.smtp.SMTPResponse smtpResponse = new org.apache.james.protocols.smtp.SMTPResponse(String.valueOf(response.getCode()), lines.get(0));
                for (int i = 1; i < lines.size(); i++) {
                    smtpResponse.appendLine(lines.get(i));
                }
                session.writeResponse(smtpResponse);
            }
            
            @Override
            public void onException(SMTPClientSession clientSession, Throwable t) {
                session.writeResponse(new org.apache.james.protocols.smtp.SMTPResponse(DSNStatus.getStatus(DSNStatus.TRANSIENT, DSNStatus.NETWORK_NO_ANSWER), "Unable to handle request"));
            }
        });
        
    }

}
