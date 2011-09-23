package me.normanmaurer.niosmtpproxy;

import java.util.List;

import me.normanmaurer.niosmtp.core.SimpleMessageInput;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

import org.apache.james.protocols.api.handler.WiringException;
import org.apache.james.protocols.smtp.MailEnvelopeImpl;
import org.apache.james.protocols.smtp.SMTPResponse;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.core.DataLineMessageHookHandler;

public class SMTPProxyDataLineHandler extends DataLineMessageHookHandler implements SMTPProxyConstants{

    @Override
    protected SMTPResponse processExtensions(SMTPSession session, MailEnvelopeImpl mail) {
        SMTPResponse response =  super.processExtensions(session, mail);
        if (response == null || Integer.parseInt(response.getRetCode()) < 400) {
            FutureSMTPResponse futureResponse = new FutureSMTPResponse();
            SMTPClientSession clientSession = (SMTPClientSession) session.getConnectionState().get(SMTP_CLIENT_SESSION_KEY);
            clientSession.send(new SimpleMessageInput(mail.getMessageInputStream()), new SMTPProxyResponseCallback(futureResponse));
            return futureResponse;
        } else {
            return response;
        }
    }

    @Override
    protected void checkMessageHookCount(List messageHandlers) throws WiringException {
        // Do nothing 
    }


}
