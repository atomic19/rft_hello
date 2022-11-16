package com.rft.tone.timer;

import com.rft.tone.timer.interfaces.RTimerCallbackInt;
import lombok.Data;

import java.util.TimerTask;

@Data
public class RTimerTask extends TimerTask {

    private final RTimerCallbackInt callback;

    @Override
    public void run() {
        callback.onTime();
    }
}
