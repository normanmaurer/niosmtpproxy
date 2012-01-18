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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import me.normanmaurer.niosmtp.SMTPResponse;

import org.apache.james.protocols.api.Response;

public class SMTPResponseAdapter implements Response {

    private final SMTPResponse response;
    private boolean endSession;

    public SMTPResponseAdapter(SMTPResponse response, boolean endSession) {
        this.response = response;
        this.endSession = endSession;
    }

    @Override
    public String getRetCode() {
        return Integer.toString(response.getCode());
    }

    @Override
    public List<CharSequence> getLines() {
        List<String> lines = response.getLines();
        if (lines == null) {
            return Collections.emptyList();
        } else {
            List<CharSequence> seqs = new ArrayList<CharSequence>(lines.size());
            Iterator<String> it = lines.iterator();
            String seperator = "-";
            while (it.hasNext()) {
                String line = it.next();
                if (!it.hasNext()) {
                    seperator = " ";
                }
                seqs.add(getRetCode() + seperator + line);
            }
            return seqs;
        }
    }

    @Override
    public boolean isEndSession() {
        return endSession;
    }

}
