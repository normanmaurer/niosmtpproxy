package me.normanmaurer.niosmtpproxy;

import java.util.Collections;
import java.util.List;

import org.apache.james.protocols.api.handler.AbstractCommandDispatcher;
import org.apache.james.protocols.api.handler.CommandHandler;
import org.apache.james.protocols.api.handler.WiringException;
import org.apache.james.protocols.smtp.SMTPSession;

public class SMTPProxyCommandDispatcher extends AbstractCommandDispatcher<SMTPSession> {

    @Override
    public void wireExtensions(Class interfaceName, List extension) throws WiringException {
        // TODO Auto-generated method stub

    }

    @Override
    protected List<String> getMandatoryCommands() {
        return Collections.EMPTY_LIST;
    }

    @Override
    protected String getUnknownCommandHandlerIdentifier() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected CommandHandler<SMTPSession> getUnknownCommandHandler() {
        // TODO Auto-generated method stub
        return null;
    }

}
