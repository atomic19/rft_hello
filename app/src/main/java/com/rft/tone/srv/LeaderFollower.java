package com.rft.tone.srv;

import com.rft.tone.config.HostConfig;
import com.rft.tone.srv.interfaces.OnMessageCallback;
import com.rft.tone.srv.lf.*;
import com.rft.tone.srv.timer.DefaultRTimer;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class LeaderFollower {
    public static void start(
            HostConfig selfConfig,
            List<HostConfig> others,
            int timeoutInSeconds) {
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
