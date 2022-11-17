package com.rft.tone.srv.lf;

import com.rft.tone.srv.Request;
import com.rft.tone.srv.interfaces.OnMessageCallbackInt;
import com.rft.tone.srv.interfaces.RTimerInt;

public class OnMessageCallback implements OnMessageCallbackInt {

    private final RTimerInt timerInt;
    public OnMessageCallback(RTimerInt timerInt) {
        this.timerInt = timerInt;
    }

    @Override
    public void onMessage(Request request) {
        this.timerInt.reset();
    }
}
