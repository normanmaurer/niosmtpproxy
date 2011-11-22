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
package me.normanmaurer.niosmtpproxy.handlers;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import me.normanmaurer.niosmtp.SMTPClientFuture;
import me.normanmaurer.niosmtp.SMTPClientFutureListener;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.SMTPRequestImpl;
import me.normanmaurer.niosmtp.transport.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;
import me.normanmaurer.niosmtpproxy.SMTPProxyConstants;
import me.normanmaurer.niosmtpproxy.SMTPResponseAdapter;

import org.apache.commons.codec.binary.Base64;
import org.apache.james.protocols.api.FutureResponseImpl;
import org.apache.james.protocols.api.Request;
import org.apache.james.protocols.api.Response;
import org.apache.james.protocols.api.handler.WiringException;
import org.apache.james.protocols.smtp.SMTPRetCode;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.core.esmtp.AuthCmdHandler;
import org.apache.james.protocols.smtp.dsn.DSNStatus;
import org.apache.james.protocols.smtp.hook.AuthHook;
import org.apache.james.protocols.smtp.hook.HookResult;
import org.apache.james.protocols.smtp.hook.HookReturnCode;

public class SMTPProxyAuthCmdHandler extends AuthCmdHandler implements SMTPProxyConstants {

    private static final Charset CHARSET = Charset.forName("US-ASCII");
    private static final SMTPRequest CANCEL_AUTH_REQUEST = new SMTPRequestImpl("*\r\n", null);
    
    @Override
    public Response onCommand(SMTPSession session, Request request) {
        final SMTPClientSession clientSession = (SMTPClientSession) session.getConnectionState().get(SMTP_CLIENT_SESSION_KEY);
        for (String extension: clientSession.getSupportedExtensions()) {
            if (extension.startsWith(AUTH_EXTENSION_PREFIX)) {
                return super.onCommand(session, request);
            }
        }
        return new org.apache.james.protocols.smtp.SMTPResponse(SMTPRetCode.SYNTAX_ERROR_COMMAND_UNRECOGNIZED, DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.DELIVERY_INVALID_CMD) +" Command " + request.getCommand() +" unrecognized.");
    }

    @Override
    protected Response doAuthTest(SMTPSession session, final String user, final String pass, String authType) {
        Response response = super.doAuthTest(session, user, pass, authType);
        int retCode = Integer.parseInt(response.getRetCode());

        // check if the return code was smaller then 400. If so we don't failed
        // the command yet and so can forward it to the real server
        if (retCode < 400) {
            final SMTPClientSession clientSession = (SMTPClientSession) session.getConnectionState().get(SMTP_CLIENT_SESSION_KEY);

            if (AUTH_TYPE_LOGIN.equals(authType)) {
                final FutureResponseImpl futureResponse = new FutureResponseImpl();
                clientSession.send(SMTPRequestImpl.authLogin()).addListener(new AuthFutureListener(futureResponse) {

                    @Override
                    protected void onOk(SMTPClientSession session, SMTPResponse response) {
                        clientSession.send(new SMTPRequestImpl(new String(Base64.encodeBase64(user.getBytes(CHARSET)), CHARSET), null)).addListener(new AuthFutureListener(futureResponse) {

                            @Override
                            protected void onOk(SMTPClientSession session, SMTPResponse response) {   
                                
                                clientSession.send(new SMTPRequestImpl(new String(Base64.encodeBase64(pass.getBytes(CHARSET)), CHARSET), null)).addListener(new AuthCompleteFutureListener(futureResponse));
                            }
                            
                        });
                        
                    }
                });
            } else if (AUTH_TYPE_PLAIN.equals(authType)) {
                final FutureResponseImpl futureResponse = new FutureResponseImpl();
                String userPass = user + "\0" + pass;
                clientSession.send(new SMTPRequestImpl(new String(Base64.encodeBase64(userPass.getBytes(CHARSET)), CHARSET), null)).addListener(new AuthCompleteFutureListener(futureResponse));
            } else {
              return new org.apache.james.protocols.smtp.SMTPResponse(SMTPRetCode.PARAMETER_NOT_IMPLEMENTED, "Unrecognized Authentication Type");

            }
        }

        return response;
    }
    
    private class AuthCompleteFutureListener extends AuthFutureListener {

        public AuthCompleteFutureListener(FutureResponseImpl futureResponse) {
            super(futureResponse);
        }

        @Override
        protected void onOk(SMTPClientSession session, SMTPResponse response) {
            futureResponse.setResponse(new SMTPResponseAdapter(response, false));
        }

        @Override
        protected Response onFail(SMTPClientSession session, SMTPResponse response) {
            return new SMTPResponseAdapter(response, false);
        }
        
    }
    
    private abstract class AuthFutureListener implements SMTPClientFutureListener<FutureResult<SMTPResponse>> {

        protected final FutureResponseImpl futureResponse;

        public AuthFutureListener(FutureResponseImpl futureResponse) {
            this.futureResponse = futureResponse;
        }
        
        @Override
        public void operationComplete(SMTPClientFuture<FutureResult<SMTPResponse>> future) {
            FutureResult<SMTPResponse> result = future.getNoWait();
            SMTPClientSession session = future.getSession();
            if (!result.isSuccess()) {
                
                futureResponse.setResponse(onException(session, result.getException()));
            } else {
                SMTPResponse response = result.getResult();
                if (response.getCode() >= 400) {
                    futureResponse.setResponse(onFail(session, response));
                } else {
                    onOk(session, response);
                }
            }
        }
        
        protected Response onException(SMTPClientSession session, Throwable t) {
            // cancel the auth on exception just in case
            session.send(CANCEL_AUTH_REQUEST);
            return new org.apache.james.protocols.smtp.SMTPResponse(SMTPRetCode.SERVICE_NOT_AVAILABLE, "Unable to handle request");
        }
        
        protected Response onFail(SMTPClientSession session, SMTPResponse response) {
            return new org.apache.james.protocols.smtp.SMTPResponse(SMTPRetCode.AUTH_FAILED, "Authentication Failed");
        }
        
        protected abstract void onOk(SMTPClientSession session, SMTPResponse response);
        
    }

    /**
     * @see org.apache.james.protocols.api.handler.ExtensibleHandler#wireExtensions(java.lang.Class, java.util.List)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void wireExtensions(Class interfaceName, List extension) throws WiringException {
        if (AuthHook.class.equals(interfaceName)) {
            List copiedExtension = new ArrayList();
            // If no AuthHook is configured then we revert to the default LocalUsersRespository check
            if (extension == null || extension.size() == 0) {
                copiedExtension.add(new AuthHook() {
                    
                    @Override
                    public HookResult doAuth(SMTPSession session, String username, String password) {
                        return new HookResult(HookReturnCode.OK);
                    }
                });
            }  else {
                copiedExtension.addAll(extension);
            }
            super.wireExtensions(interfaceName, copiedExtension);

        } else {
            super.wireExtensions(interfaceName, extension);
        }
    }
}
