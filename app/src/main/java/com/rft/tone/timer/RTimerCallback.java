package com.rft.tone.timer;

import com.rft.tone.timer.interfaces.RTimerCallbackInt;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RTimerCallback implements RTimerCallbackInt {
    @Override
    public void onTime() {
       RTimerCallback.log.info("ON TIME");
    }
}
