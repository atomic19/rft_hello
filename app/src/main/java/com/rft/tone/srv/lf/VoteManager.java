package com.rft.tone.srv.lf;

import com.rft.tone.srv.Host;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class VoteManager {
    private long termId;
    private HashMap<Host, Host> votedFor = new HashMap<>();
    private HashMap<Host, Host> voteResponse = new HashMap<>();

    public void reset(
            long termId,
            List<Host> hosts) {
        if (this.termId == termId) {
            return;
        }
        this.termId = termId;
        this.votedFor.clear();
        this.voteResponse.clear();
        hosts.forEach(e -> {
            votedFor.put(e, null);
            voteResponse.put(e, null);
        });
    }

    public boolean amILeader(Host self) {
        for (Host host : voteResponse.values()) {
            if (host == null || !host.equals(self)) {
                return false;
            }
        }
        return true;
    }
}
