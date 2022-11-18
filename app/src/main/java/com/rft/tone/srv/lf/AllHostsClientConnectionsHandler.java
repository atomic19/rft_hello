package com.rft.tone.srv.lf;

import com.rft.tone.srv.Host;
import com.rft.tone.srv.interfaces.ClientConnectionsHandler;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class AllHostsClientConnectionsHandler implements ClientConnectionsHandler {

    private final ChannelGroup channels = new DefaultChannelGroup("all-of-them", new UnorderedThreadPoolEventExecutor(5));
    private final ConcurrentHashMap<Host, Channel> hosts = new ConcurrentHashMap<>();

    @Override
    public void onChannelActive(Host host, Channel channel) {
        this.channels.add(channel);
        this.hosts.put(host, channel);
        log.info("Added channel for host: {} remoteAddress: {}", host, channel.remoteAddress());
    }

    @Override
    public void onChannelInActive(Host host, Channel channel) {
        this.hosts.remove(host);
        this.channels.remove(channel);
        log.info("Removed channel for host: {} remoteAddress: {}", host, channel.remoteAddress());
    }

    @Override
    public ChannelGroup getAllChannelsGroup() {
        return this.channels;
    }
}
