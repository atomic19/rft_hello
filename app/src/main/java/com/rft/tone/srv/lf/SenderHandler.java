package com.rft.tone.srv.lf;

import com.rft.tone.rstates.RTerm;
import com.rft.tone.srv.Host;
import com.rft.tone.srv.Request;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;
import lombok.extern.log4j.Log4j2;


// import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;
@Log4j2
public class SenderHandler extends ChannelInboundHandlerAdapter {

    private final Host self;

    public SenderHandler(Host self) {
        this.self = self;
    }

    private static final ChannelGroup channels = new DefaultChannelGroup("all-of-them", new UnorderedThreadPoolEventExecutor(5));

    public void sendToAll(Request request) {
        log.info("Sending message to all clients: {} size: {}", request, SenderHandler.channels.size());
        ChannelGroupFuture future = channels.writeAndFlush(request);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SenderHandler.channels.add(ctx.channel());
        ctx.channel().writeAndFlush( new Request(Request.RequestType.NEW_SERVER, RTerm.getTerm(), this.self) );

        log.info("Added server {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SenderHandler.channels.remove(ctx.channel());
        log.info("Removed server {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("Resetting timer i got message from server: {}", msg);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception in SenderHandler: " + ctx.channel().remoteAddress(), cause);
    }
}