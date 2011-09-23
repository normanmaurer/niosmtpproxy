package me.normanmaurer.niosmtpproxy.handlers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtpproxy.FutureSMTPResponse;

import org.apache.james.protocols.api.Request;
import org.apache.james.protocols.smtp.SMTPSession;


/**
 * {@link SMTPProxyCmdHandler} which does close the {@link SMTPSession} after processing the {@link org.apache.james.protocols.smtp.SMTPResponse}
 * 
 * @author Norman Maurer
 *
 */
public class SMTPProxyQuitCmdHandler extends SMTPProxyCmdHandler {

    private final static Collection<String> COMMANDS = Collections.unmodifiableCollection(Arrays.asList("QUIT"));


    @Override
    protected SMTPResponseCallback createCallback(final FutureSMTPResponse response, SMTPSession session, Request request, SMTPClientSession clientSession) {
        return new SMTPProxyResponseCallback(response) {

            @Override
            public void onResponse(SMTPClientSession clientSession, SMTPResponse serverResponse) {
                response.setEndSession(true);
                super.onResponse(clientSession, serverResponse);
            }
            
        };
    }


    @Override
    public Collection<String> getImplCommands() {
        return COMMANDS;
    }

}
