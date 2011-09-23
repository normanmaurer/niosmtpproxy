package me.normanmaurer.niosmtpproxy;

import java.util.Collection;
import java.util.Collections;

import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

import org.apache.james.protocols.api.Request;
import org.apache.james.protocols.api.Response;
import org.apache.james.protocols.api.handler.CommandHandler;
import org.apache.james.protocols.smtp.SMTPSession;

/**
 * {@link CommandHandler} which just forwards all {@link Request} to an remote SMTP-Server. Once it get the {@link Response} of it, it will write
 * it back the SMTP Client. 
 * 
 * So this acts as a pure proxy
 * 
 * @author Norman Maurer
 *
 */
public class SMTPProxyCmdHandler implements CommandHandler<SMTPSession>, SMTPProxyConstants {

    @Override
    public Response onCommand(SMTPSession session, final Request request) {
        final FutureSMTPResponse futureResponse = new FutureSMTPResponse();
        
        SMTPClientSession clientSession = (SMTPClientSession) session.getConnectionState().get(SMTP_CLIENT_SESSION_KEY);
        clientSession.send(new SMTPRequestImpl(request.getCommand(), request.getArgument()), createCallback(futureResponse, session, request, clientSession)); 
        return futureResponse;
    }
    
    protected SMTPResponseCallback createCallback(FutureSMTPResponse response, SMTPSession session, Request request, SMTPClientSession clientSession) {
        return new SMTPProxyResponseCallback(response);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> getImplCommands() {
        return Collections.EMPTY_LIST;
    }

}
