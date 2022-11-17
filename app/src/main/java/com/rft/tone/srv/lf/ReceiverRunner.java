package com.rft.tone.srv.lf;

import com.rft.tone.config.HostConfig;
import com.rft.tone.srv.interfaces.OnMessageCallbackInt;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ReceiverRunner implements Runnable {
    private final HostConfig host;

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final OnMessageCallbackInt onMessageCallbackInt;

    public ReceiverRunner(
            HostConfig host,
            EventLoopGroup workerGroup,
            EventLoopGroup bossGroup,
            OnMessageCallbackInt onMessageCallbackInt) {
        this.host = host;
        this.workerGroup = workerGroup;
        this.bossGroup = bossGroup;
        this.onMessageCallbackInt = onMessageCallbackInt;
    }

    @Override
    public void run() {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new ReceiverHandler(onMessageCallbackInt)
                            );
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture f = null;
        try {
            f = b.bind(this.host.getPort()).sync();
            ChannelFuture future = f.channel().closeFuture();
            log.info("*********");
            log.info("Server started at: {}", f.channel().remoteAddress());
            log.info("*********");
            future.sync();
        } catch (InterruptedException e) {
            log.error("Exception while creating server", e);
            throw new RuntimeException(e);
        }
    }
}
