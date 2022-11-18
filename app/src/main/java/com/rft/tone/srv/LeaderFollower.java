package com.rft.tone.srv;

import com.rft.tone.config.HostConfig;
import com.rft.tone.srv.interfaces.ClientConnectionsHandler;
import com.rft.tone.srv.interfaces.OnMessageCallback;
import com.rft.tone.srv.interfaces.SendMessages;
import com.rft.tone.srv.lf.AllHostsClientConnectionsHandler;
import com.rft.tone.srv.lf.DefaultSendMessage;
import com.rft.tone.srv.lf.HostOnMessageCallback;
import com.rft.tone.srv.lf.SenderReceiver;
import com.rft.tone.srv.timer.DefaultRTimer;
import com.rft.tone.srv.timer.DefaultRTimerCallback;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class LeaderFollower {
    public static void start(HostConfig selfConfig, List<HostConfig> others, int timeoutInSeconds) {
        List<Host> other = others.stream().map(e -> new Host(e.getName(), e.getPort())).collect(Collectors.toList());
        Host self = new Host(selfConfig.getName(), selfConfig.getPort());

        SenderReceiver senderReceiver = new SenderReceiver(self);
        ClientConnectionsHandler clientConnectionsHandler = new AllHostsClientConnectionsHandler(self);
        SendMessages sendMessagesInt = new DefaultSendMessage(clientConnectionsHandler);

        DefaultRTimer timer = DefaultRTimer.getInstance(
                Duration.ofSeconds(timeoutInSeconds),
                new DefaultRTimerCallback(self, sendMessagesInt, timeoutInSeconds));

        OnMessageCallback onMessageCallback = new HostOnMessageCallback(self, timer);
        senderReceiver.start(other, clientConnectionsHandler, onMessageCallback);
        timer.start();
    }
}
