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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Log4j2
public class SenderConnectionRunner implements Runnable {

    private final EventLoopGroup workerGroup;
    private final ClientConnectionsHandler clientConnectionsHandler;
    private final OnMessageCallback onMessageCallback;

    public SenderConnectionRunner(
            EventLoopGroup workerGroup,
            ClientConnectionsHandler clientConnectionsHandler,
            OnMessageCallback onMessageCallback,
            List<Host> others) {
        this.workerGroup = workerGroup;
        this.clientConnectionsHandler = clientConnectionsHandler;
        this.HOSTS.addAll(others);
        this.onMessageCallback = onMessageCallback;
    }

    /***
     * TODO:
     * an encap class that has host and channel
     * when it starts, sender connects to all known clients and stores any active channels
     * and stops sending request proactively
     * when any new client arrives, checks if it already has a channel,
     * create or fetch the host
     * if yes -> it gets replaced by the current one
     * if no -> it gets added
     *
     * create a host encap class that has both host and it's SenderChannel and ReceiverChannel
     * whenever a sender able to create a successfull connection, it will update the host with it's channel
     * and send NEW_SERVER message
     *
     * this will message will land on the Receiver of the other host
     * in the other host, Receiver will create the host encap class and send it to SenderBlockingQueue
     * SenderBlocking queue on the other server will create a new Request and send NEW_SERVER_ACK
     *
     * for now this should be testable without failing.
     *
     * we should create a new LOOP mechanism,
     * always initiate connection from sender of a host, and complete the cycle
     * sender-host-1 --NEW_SERVER---> receiver-host-2 ->
     * sender-host-2 --NEW_SERVER_ACK_1---> receiver-host-1 [CycleCompleteOnHost1] ->
     * sender-host-1 --NEW_SERVER_ACK_2---> receiver-host-2 [CycleCompleteOnHost2]
     */

    private final ArrayList<Host> HOSTS = new ArrayList<>(100);

    @Override
    public void run() {
        for (Host host2 : HOSTS) {
            try {
                final Host host = host2;

                log.info("trying to connect to host: {}", host);

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
                                new SenderHandler(host, clientConnectionsHandler, onMessageCallback)
                        );
                    }
                });

                // Start the client.
                ChannelFuture f = b.connect(name, port).sync();

                // Wait until the connection is closed.
                f.channel().closeFuture().sync();

                log.info("Connected to new host: {}", host);

            } catch (Exception ex) {
                log.info("Error while connection to host: {}", host2);
                //log.error("Exception while connecting to host:", ex);
            }
        }
    }
}
