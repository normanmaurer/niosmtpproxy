package me.normanmaurer.niosmtpproxy.handlers;

import org.apache.james.protocols.smtp.SMTPRetCode;

import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtpproxy.FutureSMTPResponse;

/**
 * {@link SMTPResponseCallback} which populate the {@link FutureSMTPResponse} and call {@link FutureSMTPResponse#markReady()}
 * 
 * @author Norman Maurer
 *
 */
public class SMTPProxyResponseCallback implements SMTPResponseCallback{

    private FutureSMTPResponse futureResponse;

    public SMTPProxyResponseCallback(FutureSMTPResponse futureResponse) {
        this.futureResponse = futureResponse;
    }
    
    @Override
    public void onResponse(SMTPClientSession clientSession, SMTPResponse serverResponse) {
        futureResponse.setRetCode(String.valueOf(serverResponse.getCode()));
        for (CharSequence seq: serverResponse.getLines()) {
            futureResponse.appendLine(seq);
        }
      
        futureResponse.markReady();
    }
    
    @Override
    public void onException(SMTPClientSession session, Throwable t) {
        futureResponse.setRetCode(SMTPRetCode.SERVICE_NOT_AVAILABLE);
        futureResponse.appendLine("Unable to handle request");
        futureResponse.setEndSession(true);
        futureResponse.markReady();
    }

}
