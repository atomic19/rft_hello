package com.rft.tone.srv;

import lombok.Data;

import java.io.Serializable;

@Data
public class Host implements Serializable {
    public String name;
    public int port;

    public Host(String name, int port) {
        this.name = name;
        this.port = port;
    }
}
