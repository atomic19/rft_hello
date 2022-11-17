package com.rft.tone.srv.lf;

import com.rft.tone.srv.Host;
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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Log4j2
public class SenderConnectionRunner implements Runnable {

    private final EventLoopGroup workerGroup;
    private final SenderHandler senderHandler;

    public SenderConnectionRunner(
            EventLoopGroup workerGroup,
            SenderHandler senderHandler) {
        this.workerGroup = workerGroup;
        this.senderHandler = senderHandler;
    }

    public final static BlockingQueue<Host> HOSTS = new ArrayBlockingQueue<>(100);

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while (true) {
            Host host = null;
            try {
                host = HOSTS.take();

                if (host.action == Host.Action.CONNECT) {
                    log.info("got new host: {}", host);
                    if (this.senderHandler.containsHost(host)) {
                        log.info("already connected to host so ignoring it: {}", host);
                    } else {
                        log.info("not previously connected to this host, trying to connect now: {}", host);
                    }

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
                                    senderHandler
                            );
                        }
                    });

                    // Start the client.
                    ChannelFuture f = b.connect(name, port).sync();

                    // Wait until the connection is closed.
                    f.channel().closeFuture().sync();

                    log.info("Connected to new host: {}", host);
                } else if (host.action == Host.Action.DISCONNECT) {
                    log.info("Disconnecting from host: {}", host);
                    this.senderHandler.disconnectHost(host);
                    log.info("Disconnected from host: {}", host);
                } else {
                    log.info("Got host with not action: {}", host);
                }
            } catch (Exception ex) {
                log.info("Error while connection to host: {}", host);
                //log.error("Exception while connecting to host:", ex);
            }
        }
    }
}
