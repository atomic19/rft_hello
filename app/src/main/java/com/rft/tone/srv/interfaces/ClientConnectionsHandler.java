package com.rft.tone.srv.interfaces;

import com.rft.tone.srv.Host;
import com.rft.tone.srv.Request;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

import java.util.List;

public interface ClientConnectionsHandler {
    void onChannelActive(Host host, Channel channel);
    void onChannelInActive(Host host, Channel channel);
    ChannelGroup getAllChannelsGroup();
    List<Host> getCurrentHosts();
    void sendMessageToHost(Host host, Request request);
}
