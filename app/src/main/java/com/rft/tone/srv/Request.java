package com.rft.tone.srv;

import lombok.Data;

import java.io.Serializable;

@Data
public class Request implements Serializable {
    public static enum RequestType {
        ACK,
        VOTE_FOR_ME,
        NEW_SERVER
    }

    private final RequestType requestType;
    private final long term;
    private final Host origin;
}
