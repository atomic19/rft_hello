package com.rft.tone.srv;

import com.rft.tone.config.HostConfig;
import com.rft.tone.db.SqlLiteCommandDb;
import com.rft.tone.srv.interfaces.OnMessageCallback;
import com.rft.tone.srv.lf.*;
import com.rft.tone.srv.timer.DefaultRTimer;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class LeaderFollower {

    private LeaderFollower(
            HostConfig selfConfig,
            List<HostConfig> others,
            int timeoutInSeconds) {
        this.selfConfig = selfConfig;
        this.others = others;
        this.timeoutInSeconds = timeoutInSeconds;
    }

    private HostConfig selfConfig;
    private List<HostConfig> others;
    private int timeoutInSeconds;

    private static LeaderFollower instance;
    public static LeaderFollower getInstance(
            HostConfig selfConfig,
            List<HostConfig> others,
            int timeoutInSeconds) {
        if (instance == null) {
            instance = new LeaderFollower(selfConfig, others, timeoutInSeconds);
        }
        return instance;
    }

    public void start() {
        List<Host> other = others.stream()
                .map(e -> new Host(e.getName(), e.getPort()))
                .collect(Collectors.toList());

        Host self = new Host(selfConfig.getName(), selfConfig.getPort());

        HostsHandler hostsHandler = new HostsHandler(self);
        SenderReceiver senderReceiver = new SenderReceiver(self);

        DefaultRTimer timer = DefaultRTimer.getInstance(Duration.ofSeconds(timeoutInSeconds), hostsHandler);

        HostOnMessageCallbackHelper onMessageCallback = new HostOnMessageCallbackHelper(self, timer);

        hostsHandler.setOnMessageCallback(onMessageCallback);
        senderReceiver.start(other, hostsHandler, hostsHandler);
        timer.start();
    }
}
