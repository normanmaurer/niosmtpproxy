package me.normanmaurer.niosmtpproxy;

import java.util.Collections;
import java.util.List;

import org.apache.james.protocols.api.handler.AbstractCommandDispatcher;
import org.apache.james.protocols.api.handler.CommandHandler;
import org.apache.james.protocols.smtp.SMTPResponse;
import org.apache.james.protocols.smtp.SMTPRetCode;
import org.apache.james.protocols.smtp.SMTPSession;

/**
 * {@link AbstractCommandDispatcher} which forwards all <code>UNKNOWN</code> commands to the "proxied" server
 * 
 * @author Norman Maurer
 *
 */
public class SMTPProxyCommandDispatcher extends AbstractCommandDispatcher<SMTPSession> implements SMTPProxyConstants {

    private final static CommandHandler<SMTPSession> UNKNOWN_CMD_HANDLER = new SMTPProxyCmdHandler();

    @Override
    public void onLine(SMTPSession session, byte[] line) {
        
        // don't handle messages till we are connected. This is kind of a workaround for the async nature but also a feature ;)
        // its called EARLYTALKER check which checks if the SMTP client tries to transmit commands before we send out the greeting.
        // This is not valid as stated in the SMTP RFC
        if (!session.getConnectionState().containsKey(SMTPProxyConstants.SMTP_CLIENT_SESSION_KEY)) {
            SMTPResponse response = new SMTPResponse(SMTPRetCode.SERVICE_NOT_AVAILABLE, "Only talk to me once I said welcome...");
            response.setEndSession(true);
            session.writeResponse(response);
        } else {
            super.onLine(session, line);
        }
    }

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
