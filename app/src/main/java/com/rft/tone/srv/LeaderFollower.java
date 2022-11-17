package com.rft.tone.srv;

import com.rft.tone.config.HostConfig;
import com.rft.tone.srv.interfaces.SendMessagesInt;
import com.rft.tone.srv.lf.SenderReceiver;
import com.rft.tone.srv.timer.RTimer;
import com.rft.tone.srv.timer.RTimerCallback;

import java.time.Duration;
import java.util.List;

public class LeaderFollower {
    public static void start(HostConfig self, List<HostConfig> others, int timeoutInSeconds) {
        SenderReceiver senderReceiver = new SenderReceiver(new Host(self.getName(), self.getPort(), Host.Action.NO_ACTION));
        SendMessagesInt sendMessagesInt = senderReceiver.createSenderRunner(others);

        RTimer timer = RTimer.getInstance(Duration.ofSeconds(timeoutInSeconds), new RTimerCallback(sendMessagesInt));
        senderReceiver.createReceiver(self, timer);
        timer.start();
    }
}
