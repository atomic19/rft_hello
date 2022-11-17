package com.rft.tone.srv.timer;

import com.rft.tone.srv.interfaces.RTimerCallbackInt;
import lombok.Data;

import java.util.TimerTask;

public class RTimerTask extends TimerTask {

    private final RTimerCallbackInt callback;

    public RTimerTask(RTimerCallbackInt callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        callback.onTime();
    }
}
