package com.rft.tone.srv.timer;

import com.rft.tone.rstates.RTerm;
import com.rft.tone.srv.Request;
import com.rft.tone.srv.interfaces.RTimerCallbackInt;
import com.rft.tone.srv.interfaces.SendMessagesInt;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
public class RTimerCallback implements RTimerCallbackInt {

    private final SendMessagesInt sendMessages;

    public RTimerCallback(SendMessagesInt sendMessages) {
        this.sendMessages = sendMessages;
    }

    @Override
    public void onTime() {
        RTimerCallback.log.info("ON TIME");
        Request request = new Request(Request.RequestType.ACK, RTerm.getTerm());
        this.sendMessages.sendMessage(request);
    }
}
