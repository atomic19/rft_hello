package com.rft.tone.srv.timer;

import com.rft.tone.rstates.RTerm;
import com.rft.tone.srv.Host;
import com.rft.tone.srv.Request;
import com.rft.tone.srv.interfaces.RTimerCallback;
import com.rft.tone.srv.interfaces.SendMessages;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
public class DefaultRTimerCallback implements RTimerCallback {

    private final SendMessages sendMessages;
    private final int timeoutInSeconds;
    private final Host self;
    public DefaultRTimerCallback(
            Host self,
            SendMessages sendMessages,
            int timeoutInSeconds) {
        this.self = self;
        this.sendMessages = sendMessages;
        this.timeoutInSeconds = timeoutInSeconds;
    }

    @Override
    public void onTime() {
        // log.info("[{}] ON TIME: {}", this.self, this.timeoutInSeconds);
        Request request = new Request(Request.RequestType.ACK, RTerm.getTerm(), this.self);
        this.sendMessages.sendMessage(request);
    }
}
