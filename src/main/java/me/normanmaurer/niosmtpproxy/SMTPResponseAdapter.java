package me.normanmaurer.niosmtpproxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import me.normanmaurer.niosmtp.SMTPResponse;

import org.apache.james.protocols.api.Response;

public class SMTPResponseAdapter implements Response{

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
