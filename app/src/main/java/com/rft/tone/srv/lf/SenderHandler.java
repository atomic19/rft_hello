package com.rft.tone.srv.lf;

import com.rft.tone.rstates.RTerm;
import com.rft.tone.srv.Host;
import com.rft.tone.srv.Request;
import com.rft.tone.srv.interfaces.ClientConnectionsHandler;
import com.rft.tone.srv.interfaces.OnMessageCallback;
import io.netty.channel.*;
import lombok.extern.log4j.Log4j2;


// import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;
@Log4j2
public class SenderHandler extends ChannelInboundHandlerAdapter {

    private final Host other;
    private final ClientConnectionsHandler clientConnectionsHandler;
    private final OnMessageCallback onMessageCallback;
    private final Host self;
    public SenderHandler(
            Host self,
            Host other,
            ClientConnectionsHandler clientConnectionsHandler,
            OnMessageCallback onMessageCallback) {
        this.self = self;
        this.other = other;
        this.clientConnectionsHandler = clientConnectionsHandler;
        this.onMessageCallback = onMessageCallback;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Request request = new Request(Request.RequestType.NEW_SERVER, RTerm.getTerm(), this.self);
        final Channel channel = ctx.channel();
        ChannelFuture future = channel.writeAndFlush(request);
        future.sync();
        log.info("{} Wrote NEW_SERVER to host: {}", self, other);
        clientConnectionsHandler.onChannelActive(other, channel);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.clientConnectionsHandler.onChannelInActive(this.other, ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("Resetting timer i got message from server: {}", msg);
        Request req = (Request) msg;
        this.onMessageCallback.onMessage(req);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception in SenderHandler: " + ctx.channel().remoteAddress(), cause);
    }
}