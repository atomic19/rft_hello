package com.rft.tone.srv.lf;

import com.rft.tone.srv.Host;
import com.rft.tone.srv.Request;
import com.rft.tone.srv.interfaces.ClientConnectionsHandler;
import com.rft.tone.srv.interfaces.OnMessageCallback;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;


@Log4j2
public class ReceiverHandler extends ChannelInboundHandlerAdapter {

    private final OnMessageCallback onMessageCallbackInt;
    private final ClientConnectionsHandler clientConnectionsHandler;

    public ReceiverHandler(
            ClientConnectionsHandler clientConnectionsHandler,
            OnMessageCallback onMessageCallbackInt) {
        this.clientConnectionsHandler = clientConnectionsHandler;
        this.onMessageCallbackInt = onMessageCallbackInt;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Host host = SenderReceiver.getHostFromChannel(ctx.channel());
        this.clientConnectionsHandler.onChannelActive(host, ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Host host = SenderReceiver.getHostFromChannel(ctx.channel());
        this.clientConnectionsHandler.onChannelInActive(host, ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Host host = SenderReceiver.getHostFromChannel(ctx.channel());
        Request req = (Request) msg;
        this.onMessageCallbackInt.onMessage(req, host);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
