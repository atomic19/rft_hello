package com.rft.tone.srv.lf;

import com.rft.tone.srv.Request;
import com.rft.tone.srv.interfaces.ClientConnectionsHandler;
import com.rft.tone.srv.interfaces.SendMessages;
import io.netty.channel.group.ChannelGroup;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DefaultSendMessage implements SendMessages {

    private final ClientConnectionsHandler clientConnectionsHandler;

    public DefaultSendMessage(
            ClientConnectionsHandler clientConnectionsHandler) {
        this.clientConnectionsHandler = clientConnectionsHandler;
    }

    @Override
    public void sendMessage(Request request) {
        ChannelGroup channels = this.clientConnectionsHandler.getAllChannelsGroup();
        log.info("Sending message to all clients: {} size: {}", request, channels.size());
        channels.writeAndFlush(request);
    }
}
