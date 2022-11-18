package com.rft.tone.srv.lf;

import com.rft.tone.srv.Host;
import com.rft.tone.srv.Request;
import com.rft.tone.srv.interfaces.ClientConnectionsHandler;
import com.rft.tone.srv.interfaces.OnMessageCallback;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;


// import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;
@Log4j2
public class SenderHandler extends ChannelInboundHandlerAdapter {

    private final Host other;
    private final ClientConnectionsHandler clientConnectionsHandler;
    private final OnMessageCallback onMessageCallback;

    public SenderHandler(
            Host other,
            ClientConnectionsHandler clientConnectionsHandler,
            OnMessageCallback onMessageCallback) {
        this.other = other;
        this.clientConnectionsHandler = clientConnectionsHandler;
        this.onMessageCallback = onMessageCallback;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.clientConnectionsHandler.onChannelActive(this.other, ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.clientConnectionsHandler.onChannelInActive(this.other, ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("Resetting timer i got message from server: {}", msg);
        Request req = (Request) msg;
        this.onMessageCallback.onMessage(req, this.other);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception in SenderHandler: " + ctx.channel().remoteAddress(), cause);
    }
}