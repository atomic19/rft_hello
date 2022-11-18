package com.rft.tone.srv.interfaces;

import java.time.Duration;

public interface RTimer {
    void start();
    void reset();
    void updateTimer(Duration duration);
}
