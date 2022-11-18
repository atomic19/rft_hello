package com.rft.tone.rstates;

import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

@Data
public class RTerm {
    private static AtomicLong term = new AtomicLong(1L);

    public static long getTerm() {
        return RTerm.term.get();
    }

    public static void incTerm() {
        RTerm.term.incrementAndGet();
    }
}
