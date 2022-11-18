package com.rft.tone.srv.interfaces;

import com.rft.tone.srv.Host;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

public interface ClientConnectionsHandler {
    void onChannelActive(Host host, Channel channel);
    void onChannelInActive(Host host, Channel channel);

    ChannelGroup getAllChannelsGroup();
}
