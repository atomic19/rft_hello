package com.rft.tone.cmdsrv;

import lombok.Data;

import java.io.Serializable;

@Data
public class Command implements Serializable {
    private String command;
    private long slotId;
    private long termId;
    private long commandHash;
    private boolean isCommitted;
}
