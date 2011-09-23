package me.normanmaurer.niosmtpproxy.handlers;

import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtpproxy.FutureSMTPResponse;
import me.normanmaurer.niosmtpproxy.SMTPProxyConstants;

import org.apache.james.protocols.smtp.SMTPResponse;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.core.DataCmdHandler;

@SuppressWarnings("unchecked")
public class SMTPProxyDataCmdHandler extends DataCmdHandler implements SMTPProxyConstants{

    @Override
    protected SMTPResponse doDATA(final SMTPSession session, String argument) {
        SMTPResponse response =   super.doDATA(session, argument);
        int retCode = Integer.parseInt(response.getRetCode());
        
        // check if the return code was smaller then 400. If so we don't failed the command yet and so can forward it to the real server
        if (retCode < 400) {
            FutureSMTPResponse futureResponse = new FutureSMTPResponse();
            SMTPClientSession clientSession = (SMTPClientSession) session.getConnectionState().get(SMTP_CLIENT_SESSION_KEY);
            clientSession.send(SMTPRequestImpl.data(), new ExtensibleSMTPProxyResponseCallback(session, futureResponse){

                @Override
                public void onResponse(SMTPClientSession clientSession, me.normanmaurer.niosmtp.SMTPResponse serverResponse) {
                    super.onResponse(clientSession, serverResponse);
                }

                @Override
                protected void onFailure(SMTPSession session, SMTPClientSession clientSession) {
                    session.getState().remove(MAILENV);
                    session.popLineHandler();
                }

            });
            return futureResponse;
            
        } else {
            return response;
        }
    }


}
