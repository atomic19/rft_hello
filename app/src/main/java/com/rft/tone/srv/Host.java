package com.rft.tone.srv;

import lombok.Data;

@Data
public class Host {
    public String name;
    public int port;
    public Action action;

    public Host(String name, int port, Action action) {
        this.name = name;
        this.port = port;
        this.action = action;
    }

    public static enum Action {
        CONNECT,
        DISCONNECT,
        NO_ACTION
    }
}
