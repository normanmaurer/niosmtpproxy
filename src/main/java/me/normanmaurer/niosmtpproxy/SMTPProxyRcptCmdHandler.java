package me.normanmaurer.niosmtpproxy;

import java.util.Collection;

import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

import org.apache.james.protocols.smtp.SMTPResponse;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.core.RcptCmdHandler;
import org.apache.mailet.MailAddress;

public class SMTPProxyRcptCmdHandler extends RcptCmdHandler implements SMTPProxyConstants{

    @Override
    protected SMTPResponse doCoreCmd(final SMTPSession session, String command, String parameters) {
        SMTPResponse response =  super.doCoreCmd(session, command, parameters);
        int retCode = Integer.parseInt(response.getRetCode());
        
        if (retCode < 400) {
            FutureSMTPResponse futureResponse = new FutureSMTPResponse();
            
            SMTPClientSession clientSession = (SMTPClientSession) session.getConnectionState().get(SMTP_CLIENT_SESSION_KEY);
            final MailAddress rcpt = (MailAddress) session.getState().get(CURRENT_RECIPIENT);
            clientSession.send(SMTPRequestImpl.rcpt(rcpt.toString()), new ExtensibleSMTPProxyResponseCallback(session, futureResponse){

                @SuppressWarnings("unchecked")
                @Override
                protected void onFailure(SMTPSession session, SMTPClientSession clientSession) {
                    Collection<MailAddress> recpients = (Collection<MailAddress>) session.getState().get(SMTPSession.RCPT_LIST);
                    recpients.remove(rcpt);                    
                }

                
            });
            return futureResponse;
            
        } else {
            return response;
        }
    }

}
