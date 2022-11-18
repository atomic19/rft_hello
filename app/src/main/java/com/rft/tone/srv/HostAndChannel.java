package com.rft.tone.srv;

import io.netty.channel.Channel;
import lombok.Data;

@Data
public class HostAndChannel {
    public Host host;
    public Channel channel;
}
