package com.rft.tone.rstates;

import lombok.Data;
import lombok.Synchronized;

@Data
public class RTerm {
    private static final Object termLock = new Object();
    private static long term = 1L;

    @Synchronized("termLock")
    public static long getTerm() {
        return RTerm.term;
    }

    @Synchronized("termLock")
    public static long incTerm() {
        RTerm.term += 1;
        return RTerm.term;
    }
}
