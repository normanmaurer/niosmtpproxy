package me.normanmaurer.niosmtpproxy;

import org.apache.james.protocols.smtp.SMTPSession;

import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

public abstract class ExtensibleSMTPProxyResponseCallback extends SMTPProxyResponseCallback{

    private final SMTPSession session;


    public ExtensibleSMTPProxyResponseCallback(SMTPSession session, FutureSMTPResponse futureResponse) {
        super(futureResponse);
        this.session = session;
    }
    

    @Override
    public void onResponse(SMTPClientSession clientSession, SMTPResponse serverResponse) {
        if (serverResponse.getCode() >= 400) {
            onFailure(session, clientSession);
        }
        super.onResponse(clientSession, serverResponse);
    }

    @Override
    public void onException(SMTPClientSession clientSession, Throwable t) {
        onFailure(session, clientSession);
        super.onException(clientSession, t);
    }

    
    /**
     * Gets called on an {@link Exception} or when {@link SMTPResponse#getCode()} returns a PERM or TEMP error
     * 
     * @param session
     * @param clientSession
     */
    protected abstract void onFailure(SMTPSession session, SMTPClientSession clientSession);
}
