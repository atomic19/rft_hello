package com.rft.tone.srv.lf;

import com.rft.tone.srv.Host;
import com.rft.tone.srv.Request;
import com.rft.tone.srv.interfaces.OnMessageCallback;
import com.rft.tone.srv.interfaces.RTimer;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class HostOnMessageCallback implements OnMessageCallback {

    private final RTimer timer;
    public HostOnMessageCallback(RTimer timer) {
        this.timer = timer;
    }

    @Override
    public void onMessage(Request request, Host host) {
        log.info("Resetting timer as Host: {} SentData: {}", host, request);
        this.timer.reset();
    }
}
