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

    private Host host = null;
    private final Host self;
    private final OnMessageCallback onMessageCallbackInt;
    private final ClientConnectionsHandler clientConnectionsHandler;

    public ReceiverHandler(
            Host self,
            ClientConnectionsHandler clientConnectionsHandler,
            OnMessageCallback onMessageCallbackInt) {
        this.self = self;
        this.clientConnectionsHandler = clientConnectionsHandler;
        this.onMessageCallbackInt = onMessageCallbackInt;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("{} Active Channel but not adding", this.self);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(this.host != null) {
            this.clientConnectionsHandler.onChannelInActive(host, ctx.channel());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Request req = (Request) msg;
        log.info("{} Message: {}", this.self, req);
        if (req.getRequestType() == Request.RequestType.NEW_SERVER) {
            this.host = req.getOrigin();
            this.clientConnectionsHandler.onChannelActive(this.host, ctx.channel());
        } else if (this.host != null) {
            this.onMessageCallbackInt.onMessage(req, this.host);
        } else {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
