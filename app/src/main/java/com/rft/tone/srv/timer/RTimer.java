package com.rft.tone.srv.timer;

import com.rft.tone.srv.interfaces.RTimerCallbackInt;
import com.rft.tone.srv.interfaces.RTimerInt;
import lombok.Data;

import java.time.Duration;
import java.util.Timer;

@Data
public class RTimer implements RTimerInt {

    private volatile Timer timer;
    private final RTimerCallbackInt callback;
    private final long timerInMs;
    private final Object rTimerLock = new Object();

    private RTimer(long timerInMs, RTimerCallbackInt callback) {
        this.callback = callback;
        this.timerInMs = timerInMs;
    }

    private volatile static RTimer instance;
    private static final Object rTimerInstanceLock = new Object();

    public static RTimer getInstance(Duration duration, RTimerCallbackInt callback) {
        RTimer result = instance;
        if (result == null) {
            synchronized (rTimerInstanceLock) {
                result = instance;
                if (result == null) {
                    result = instance = new RTimer(duration.toMillis(), callback);
                }
            }
        }
        return result;
    }

    @Override
    public void start() {
        if (!this.startAndScheduleTimer()) {
            throw new RuntimeException("timer already started");
        }
    }

    @Override
    public void reset() {
        this.cancelCreateAndScheduleTimer();
    }

    private boolean startAndScheduleTimer() {
        boolean created = false;
        Timer res = this.timer;
        if (res == null) {
            synchronized (this.rTimerLock) {
                res = this.timer;
                if (res == null) {
                    this.timer = new Timer(true);
                    this.timer.scheduleAtFixedRate(new RTimerTask(this.callback), 0, this.timerInMs);
                    created = true;
                }
            }
        }
        return created;
    }

    private void cancelCreateAndScheduleTimer() {
        Timer res = this.timer;
        if (res != null) {
            synchronized (this.rTimerLock) {
                res = this.timer;
                if (res != null) {
                    this.timer.cancel();
                    res = this.timer = new Timer(true);
                    this.timer.scheduleAtFixedRate(new RTimerTask(this.callback), 0, this.timerInMs);
                }
            }
        }
    }
}
