package me.normanmaurer.niosmtpproxy.handlers;


import org.apache.james.protocols.smtp.MailEnvelope;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.hook.HookResult;
import org.apache.james.protocols.smtp.hook.HookReturnCode;
import org.apache.james.protocols.smtp.hook.MessageHook;

public class SMTPProxyAcceptingMessageHook implements MessageHook{

    @Override
    public HookResult onMessage(SMTPSession session, MailEnvelope mail) {
        
        return new HookResult(HookReturnCode.OK);
    }

}
