package com.rft.tone.srv.lf;

import com.rft.tone.srv.Host;
import com.rft.tone.srv.interfaces.ClientConnectionsHandler;
import com.rft.tone.srv.interfaces.OnMessageCallback;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class SenderConnectionRunner implements Runnable {

    private final EventLoopGroup workerGroup;
    private final ClientConnectionsHandler clientConnectionsHandler;
    private final OnMessageCallback onMessageCallback;

    private final Host self;
    private final Host other;

    public SenderConnectionRunner(
            Host self,
            EventLoopGroup workerGroup,
            ClientConnectionsHandler clientConnectionsHandler,
            OnMessageCallback onMessageCallback,
            Host other) {
        this.self = self;
        this.workerGroup = workerGroup;
        this.clientConnectionsHandler = clientConnectionsHandler;
        this.other = other;
        this.onMessageCallback = onMessageCallback;
    }

    @Override
    public void run() {

        try {
            final Host host = this.other;

            log.info("*********trying to connect to host: {}", host);

            String name = host.getName();
            int port = host.getPort();

            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                            new ObjectEncoder(),
                            new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                            new SenderHandler(self, host, clientConnectionsHandler, onMessageCallback)
                    );
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(name, port).sync();

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();

            log.info("Connected to new host: {}", host);

        } catch (Exception ex) {
            log.info("Error while connection to host: {}", this.other);
            //log.error("Exception while connecting to host:", ex);
        }
    }

}
