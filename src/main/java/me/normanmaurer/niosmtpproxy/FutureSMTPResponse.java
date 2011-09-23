package me.normanmaurer.niosmtpproxy;

import java.util.ArrayList;
import java.util.List;

import org.apache.james.protocols.api.FutureResponse;
import org.apache.james.protocols.smtp.SMTPResponse;

/**
 * {@link SMTPResponse} sub-type which allows to set the response in an async fashion.
 * 
 * The user of this implementation MUST make sure to call {@link #markReady()} once he is done.
 * Otherwise callers MAY block forever
 * 
 * @author Norman Maurer
 *
 */
public class FutureSMTPResponse extends SMTPResponse implements FutureResponse{

    private boolean ready = false;
    private List<ResponseListener> listeners = new ArrayList<ResponseListener>();
    


    public FutureSMTPResponse() {
    }

    private synchronized void checkReady() {
        while (!ready) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    @Override
    public synchronized void addListener(ResponseListener listener) {
        listeners.add(listener);
        if (ready) {
            listener.onResponse(this);
        }
    }

    @Override
    public synchronized void removeListener(ResponseListener listener) {
        listeners.remove(listener);
    }

    @Override
    public synchronized boolean isReady() {
        return ready;
    }
    
    public synchronized void markReady() {
        if (!ready) {
            ready = true;
            notify();
            for (ResponseListener listener: listeners) {
                listener.onResponse(this);
            }
        }
    }

    @Override
    public List<CharSequence> getLines() {
        checkReady();
        return super.getLines();
    }

    @Override
    public synchronized void appendLine(CharSequence line) {
        if (ready) {
            throw new IllegalStateException("FutureSMTPResponse MUST NOT get modified after its ready");
        }
        super.appendLine(line);
    }

    @Override
    public String getRetCode() {
        checkReady();
        return super.getRetCode();
    }

    @Override
    public synchronized void setRetCode(String retCode) {
        if (ready) {
            throw new IllegalStateException("FutureSMTPResponse MUST NOT get modified after its ready");
        }
        super.setRetCode(retCode);
    }

    @Override
    public String getRawLine() {
        checkReady();
        return super.getRawLine();
    }

    @Override
    public boolean isEndSession() {
        checkReady();
        return super.isEndSession();
    }

    @Override
    public synchronized void setEndSession(boolean endSession) {
        if (ready) {
            throw new IllegalStateException("FutureSMTPResponse MUST NOT get modified after its ready");
        }
        super.setEndSession(endSession);
    }

}
