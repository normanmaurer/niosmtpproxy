package me.normanmaurer.niosmtpproxy;

import org.apache.james.protocols.smtp.dsn.DSNStatus;

import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

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
        futureResponse.setRetCode(DSNStatus.getStatus(DSNStatus.TRANSIENT, DSNStatus.NETWORK_NO_ANSWER));
        futureResponse.appendLine("Unable to handle request");
        futureResponse.setEndSession(true);
        futureResponse.markReady();
    }

}
