package com.rft.tone.srv.lf;

import com.rft.tone.config.HostConfig;
import com.rft.tone.srv.Host;
import com.rft.tone.srv.interfaces.RTimerInt;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

public class SenderReceiver {

    private final SenderHandler senderHandler;
    private final EventLoopGroup workerGroup;
    private final EventLoopGroup bossGroup;
    public SenderReceiver(Host self) {
        this.senderHandler = new SenderHandler(self);
        this.workerGroup = new NioEventLoopGroup();

        this.bossGroup = new NioEventLoopGroup();
    }

    public SendMessage createSenderRunner(List<HostConfig> others) {
        List<Host> other = others.stream().map(e -> new Host(e.getName(), e.getPort(), Host.Action.CONNECT)).collect(Collectors.toList());
        SenderConnectionRunner.HOSTS.addAll(other);
        SenderConnectionRunner senderConnectionRunner = new SenderConnectionRunner(workerGroup, senderHandler);
        new Thread(senderConnectionRunner).start();
        return new SendMessage(this.senderHandler);
    }

    public void createReceiver(HostConfig self, RTimerInt rTimer) {
        ReceiverRunner receiverRunner = new ReceiverRunner(self, workerGroup, bossGroup, new OnMessageCallback(rTimer));
        new Thread(receiverRunner).start();
    }

    public static Host getHostFromChannel(Channel channel) {
        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        return new Host(address.getAddress().getHostAddress(), address.getPort(), Host.Action.NO_ACTION);
    }
}
