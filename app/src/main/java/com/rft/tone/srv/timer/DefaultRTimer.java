package com.rft.tone.srv.timer;

import com.rft.tone.srv.interfaces.RTimerCallback;
import com.rft.tone.srv.interfaces.RTimer;
import lombok.Data;

import java.time.Duration;
import java.util.Timer;

@Data
public class DefaultRTimer implements RTimer {

    private volatile Timer timer;
    private final RTimerCallback callback;
    private final long timerInMs;
    private final Object rTimerLock = new Object();

    private DefaultRTimer(long timerInMs, RTimerCallback callback) {
        this.callback = callback;
        this.timerInMs = timerInMs;
    }

    private volatile static DefaultRTimer instance;
    private static final Object rTimerInstanceLock = new Object();

    public static DefaultRTimer getInstance(Duration duration, RTimerCallback callback) {
        DefaultRTimer result = instance;
        if (result == null) {
            synchronized (rTimerInstanceLock) {
                result = instance;
                if (result == null) {
                    result = instance = new DefaultRTimer(duration.toMillis(), callback);
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
                    this.timer.scheduleAtFixedRate(new DefaultRTimerTask(this.callback), this.timerInMs, this.timerInMs);
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
                    this.timer.scheduleAtFixedRate(new DefaultRTimerTask(this.callback), this.timerInMs, this.timerInMs);
                }
            }
        }
    }
}
