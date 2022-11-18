package com.rft.tone.srv.lf;

import com.rft.tone.srv.Host;
import com.rft.tone.srv.interfaces.ClientConnectionsHandler;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class AllHostsClientConnectionsHandler implements ClientConnectionsHandler {

    private final ChannelGroup channels = new DefaultChannelGroup("all-of-them", new UnorderedThreadPoolEventExecutor(5));
    private final ConcurrentHashMap<Host, Channel> hosts = new ConcurrentHashMap<>();
    private final Host self;
    public AllHostsClientConnectionsHandler(Host self) {
        this.self = self;
    }

    @Override
    public void onChannelActive(Host host, Channel channel) {
        this.channels.add(channel);
        this.hosts.put(host, channel);
        log.info("Added channel for host: {} remoteAddress: {}", host, channel.remoteAddress());
        //log.info("{} [Add] All my hosts: {}", this.self, Arrays.toString(this.getHosts().toArray()));
    }

    @Override
    public void onChannelInActive(Host host, Channel channel) {
        this.hosts.remove(host);
        this.channels.remove(channel);
        log.info("Removed channel for host: {} remoteAddress: {}", host, channel.remoteAddress());
        //log.info("{} [Rem] All my hosts: {}", this.self, Arrays.toString(this.getHosts().toArray()));
    }

    @Override
    public ChannelGroup getAllChannelsGroup() {
        // log.info("{} [Q] All my hosts: {}", this.self, Arrays.toString(this.getHosts().toArray()));
        return this.channels;
    }

    private List<Host> getHosts() {
        List<Host> list = new ArrayList<>();
        this.hosts.keys().asIterator().forEachRemaining(list::add);
        return list;
    }
}
