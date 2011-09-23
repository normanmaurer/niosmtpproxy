package me.normanmaurer.niosmtpproxy.handlers;

import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtpproxy.FutureSMTPResponse;
import me.normanmaurer.niosmtpproxy.SMTPProxyConstants;

import org.apache.james.protocols.smtp.SMTPResponse;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.core.MailCmdHandler;
import org.apache.mailet.MailAddress;

public class SMTPProxyMailCmdHandler extends MailCmdHandler implements SMTPProxyConstants{

    @Override
    protected SMTPResponse doCoreCmd(final SMTPSession session, String command, String parameters) {
        SMTPResponse response =  super.doCoreCmd(session, command, parameters);
        int retCode = Integer.parseInt(response.getRetCode());
        
        // check if the return code was smaller then 400. If so we don't failed the command yet and so can forward it to the real server
        if (retCode < 400) {
            FutureSMTPResponse futureResponse = new FutureSMTPResponse();
            SMTPClientSession clientSession = (SMTPClientSession) session.getConnectionState().get(SMTP_CLIENT_SESSION_KEY);
            MailAddress mailFrom = (MailAddress) session.getState().get(SMTPSession.SENDER);
            
            String sender;
            // check for null sender
            if (mailFrom == null) {
                sender = "";
            } else {
                sender = mailFrom.toString();
            }
            clientSession.send(SMTPRequestImpl.mail(sender), new ExtensibleSMTPProxyResponseCallback(session, futureResponse){

                @Override
                protected void onFailure(SMTPSession session, SMTPClientSession clientSession) {
                    session.getState().remove(SMTPSession.SENDER);                    
                }

            });
            return futureResponse;
            
        } else {
            return response;
        }
    }

}
