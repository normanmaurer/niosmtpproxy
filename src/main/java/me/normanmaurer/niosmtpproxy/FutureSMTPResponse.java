/**
* Licensed to niosmtpproxy developers ('niosmtpproxy') under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* niosmtpproxy licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package me.normanmaurer.niosmtpproxy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

    private volatile boolean ready = false;
    private List<ResponseListener> listeners = new CopyOnWriteArrayList<ResponseListener>();
    


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
    public void addListener(ResponseListener listener) {
        listeners.add(listener);
        if (ready) {
            listener.onResponse(this);
        }
    }

    @Override
    public void removeListener(ResponseListener listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean isReady() {
        return ready;
    }
    
    public void markReady() {
        if (!ready) {
            ready = true;
            synchronized (this) {
                notify();
            }
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
    public void appendLine(CharSequence line) {
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
    public void setRetCode(String retCode) {
        if (ready) {
            throw new IllegalStateException("FutureSMTPResponse MUST NOT get modified after its ready");
        }
        super.setRetCode(retCode);
    }

    @Override
    public boolean isEndSession() {
        checkReady();
        return super.isEndSession();
    }

    @Override
    public void setEndSession(boolean endSession) {
        if (ready) {
            throw new IllegalStateException("FutureSMTPResponse MUST NOT get modified after its ready");
        }
        super.setEndSession(endSession);
    }

}
