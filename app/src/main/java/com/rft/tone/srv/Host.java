package com.rft.tone.srv;

import lombok.Data;

@Data
public class Host {
    public String name;
    public int port;

    public Host(String name, int port) {
        this.name = name;
        this.port = port;
    }
}
