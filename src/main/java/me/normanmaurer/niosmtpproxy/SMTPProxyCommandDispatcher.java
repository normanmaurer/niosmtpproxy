package me.normanmaurer.niosmtpproxy;

import java.util.Collections;
import java.util.List;

import org.apache.james.protocols.api.handler.AbstractCommandDispatcher;
import org.apache.james.protocols.api.handler.CommandHandler;
import org.apache.james.protocols.smtp.SMTPSession;

/**
 * {@link AbstractCommandDispatcher} which forwards all <code>UNKNOWN</code> commands to the "proxied" server
 * 
 * @author Norman Maurer
 *
 */
public class SMTPProxyCommandDispatcher extends AbstractCommandDispatcher<SMTPSession> {

    private final static CommandHandler<SMTPSession> UNKNOWN_CMD_HANDLER = new SMTPProxyCmdHandler();

    @SuppressWarnings("unchecked")
    @Override
    protected List<String> getMandatoryCommands() {
        return Collections.EMPTY_LIST;
    }

    @Override
    protected String getUnknownCommandHandlerIdentifier() {
        return SMTPProxyCmdHandler.class.getName();
    }

    @Override
    protected CommandHandler<SMTPSession> getUnknownCommandHandler() {
        return UNKNOWN_CMD_HANDLER;
    }

}
