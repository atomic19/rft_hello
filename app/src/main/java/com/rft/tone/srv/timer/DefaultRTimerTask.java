package com.rft.tone.srv.timer;

import com.rft.tone.srv.interfaces.RTimerCallback;

import java.util.TimerTask;

public class DefaultRTimerTask extends TimerTask {

    private final RTimerCallback callback;

    public DefaultRTimerTask(RTimerCallback callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        callback.onTime();
    }
}
