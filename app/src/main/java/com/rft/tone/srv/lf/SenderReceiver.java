package com.rft.tone.srv.lf;

import com.rft.tone.srv.Host;
import com.rft.tone.srv.interfaces.ClientConnectionsHandler;
import com.rft.tone.srv.interfaces.OnMessageCallback;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.List;

public class SenderReceiver {

    private final EventLoopGroup workerGroup;
    private final EventLoopGroup bossGroup;
    private final Host self;
    public SenderReceiver(Host self) {
        this.workerGroup = new NioEventLoopGroup();
        this.bossGroup = new NioEventLoopGroup();
        this.self = self;
    }

    public void start(List<Host> others,
                      ClientConnectionsHandler clientConnectionsHandler,
                      OnMessageCallback onMessageCallback) {

        for (Host other : others) {
            SenderConnectionRunner senderConnectionRunner = new SenderConnectionRunner(
                    this.self,
                    workerGroup,
                    clientConnectionsHandler,
                    onMessageCallback,
                    other);
            new Thread(senderConnectionRunner).start();
        }

        ReceiverRunner receiverRunner = new ReceiverRunner(
                this.self,
                this.workerGroup,
                this.bossGroup,
                clientConnectionsHandler,
                onMessageCallback);
        new Thread(receiverRunner).start();
    }
}
