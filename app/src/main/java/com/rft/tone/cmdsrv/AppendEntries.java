package com.rft.tone.cmdsrv;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

@Data
public class AppendEntries implements Serializable {
    private ArrayList<Command> entries;
    private Command entry;
}
