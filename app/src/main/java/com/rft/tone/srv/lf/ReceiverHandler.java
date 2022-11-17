package com.rft.tone.srv.lf;

import com.rft.tone.srv.Host;
import com.rft.tone.srv.Request;
import com.rft.tone.srv.interfaces.OnMessageCallbackInt;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;


@Log4j2
public class ReceiverHandler extends ChannelInboundHandlerAdapter {

    private final OnMessageCallbackInt onMessageCallbackInt;

    public ReceiverHandler(OnMessageCallbackInt onMessageCallbackInt) {
        this.onMessageCallbackInt = onMessageCallbackInt;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Host host = SenderReceiver.getHostFromChannel(ctx.channel());
        host.setAction(Host.Action.CONNECT);
        SenderConnectionRunner.HOSTS.add(host);
        log.info("*****Queued new client {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Host host = SenderReceiver.getHostFromChannel(ctx.channel());
        host.setAction(Host.Action.DISCONNECT);
        log.info("****Disconnected client {}", host);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("*New Message data {}", msg);
        Request req = (Request) msg;
        if (req.getRequestType() == Request.RequestType.NEW_SERVER) {
            Host otherHost = req.getSelf();
            SenderConnectionRunner.HOSTS.add(otherHost);
        } else {
            this.onMessageCallbackInt.onMessage(req);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
