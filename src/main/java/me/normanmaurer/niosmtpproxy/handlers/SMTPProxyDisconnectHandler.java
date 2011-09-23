package me.normanmaurer.niosmtpproxy.handlers;

import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtpproxy.SMTPProxyConstants;

import org.apache.james.protocols.api.handler.DisconnectHandler;
import org.apache.james.protocols.smtp.SMTPSession;

/**
 * {@link DisconnectHandler} which makes sure the {@link SMTPClientSession} get disconnected
 * 
 * @author Norman Maurer
 *
 */
public class SMTPProxyDisconnectHandler implements DisconnectHandler<SMTPSession>, SMTPProxyConstants{

    @Override
    public void onDisconnect(SMTPSession session) {
        SMTPClientSession clientSession = (SMTPClientSession) session.getConnectionState().get(SMTP_CLIENT_SESSION_KEY);
        clientSession.close();
    }

}
