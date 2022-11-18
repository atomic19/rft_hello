package com.rft.tone.srv.lf;

import com.rft.tone.rstates.RTerm;
import com.rft.tone.srv.Host;
import com.rft.tone.srv.Request;
import com.rft.tone.srv.interfaces.ClientConnectionsHandler;
import com.rft.tone.srv.interfaces.OnMessageCallback;
import com.rft.tone.srv.interfaces.RTimerCallback;
import com.rft.tone.srv.interfaces.SendMessages;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Log4j2
public class HostsHandler implements
        OnMessageCallback,
        ClientConnectionsHandler,
        SendMessages, RTimerCallback {

    private final ChannelGroup channels = new DefaultChannelGroup("all-of-them", new UnorderedThreadPoolEventExecutor(5));
    private final ConcurrentHashMap<Host, Channel> hosts = new ConcurrentHashMap<>();
    private Host self;
    private HostOnMessageCallbackHelper onMessageCallback;
    private SendMessages sendMessages;
    public HostsHandler(Host self) {
        this.self = self;
    }

    public void setOnMessageCallback(HostOnMessageCallbackHelper onMessageCallback) {
        onMessageCallback.setClientConnectionsHandler(this);
        onMessageCallback.setSendMessages(this);
        this.onMessageCallback = onMessageCallback;
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

    @Override
    public void onMessage(Request request) {
        this.onMessageCallback.onMessage(request);
    }

    @Override
    public void sendMessage(Request request) {
        ChannelGroup channels = this.getAllChannelsGroup();
        log.info("Sending message to all clients: {} size: {}", request, channels.size());
        ChannelGroupFuture future = channels.writeAndFlush(request);
    }

    @Override
    public void onTime() {
        if(this.onMessageCallback != null) {
            this.onMessageCallback.handleOnTime();
        }
    }

    @Override
    public List<Host> getCurrentHosts() {
        List<Host> list = new ArrayList<>();
        this.hosts.keys().asIterator().forEachRemaining(list::add);
        return list;
    }

    @Override
    public void sendMessageToHost(Host host, Request request) {
        this.hosts.get(host).writeAndFlush(request);
    }
}
