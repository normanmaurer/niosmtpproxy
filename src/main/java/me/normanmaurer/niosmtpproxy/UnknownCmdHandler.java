package me.normanmaurer.niosmtpproxy;

import java.util.Collection;
import java.util.Collections;

import org.apache.james.protocols.api.Request;
import org.apache.james.protocols.api.Response;
import org.apache.james.protocols.api.handler.CommandHandler;
import org.apache.james.protocols.smtp.SMTPSession;

public class UnknownCmdHandler implements CommandHandler<SMTPSession>{

    @Override
    public Response onCommand(SMTPSession session, Request request) {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> getImplCommands() {
        return Collections.EMPTY_LIST;
    }

}
