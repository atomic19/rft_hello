package com.rft.tone.srv.lf;

import com.rft.tone.App;
import com.rft.tone.rstates.RState;
import com.rft.tone.rstates.RTerm;
import com.rft.tone.srv.Host;
import com.rft.tone.srv.Request;
import com.rft.tone.srv.interfaces.OnMessageCallback;
import com.rft.tone.srv.interfaces.RTimer;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;

@Log4j2
public class HostOnMessageCallback implements OnMessageCallback {

    private volatile RState myState;
    private volatile Host leader;
    private final Host self;
    private final RTimer timer;

    private final Object stateLock = new Object();

    public HostOnMessageCallback(Host self, RTimer timer) {
        this.self = self;
        this.timer = timer;
        this.myState = RState.CANDIDATE;
    }

    @Override
    public void onMessage(Request request) {
        synchronized (this.stateLock) {
            switch (request.getRequestType()) {
                case ACK:
                    this.respondToAck(request);
                    break;
                case VOTE_FOR_ME:
                    this.respondToVoteForMe(request);
                    break;
                default:
                    log.info("{} Unknown state: {}", self, request);
            }
        }
    }

    private void respondToVoteForMe(Request request) {
        if (RTerm.getTerm() > request.getTerm()) {
            // I have higher term
            // I will not vote and stay as is and ignore this person
            log.info("[{}] a host {} is asking for my vote, but i refuse to give it as i'm ", this.self, request.getOrigin());
            return;
        } else if (RTerm.getTerm() < request.getTerm()) {
            // my term is lower, updating my term the requested on and voting
        }

        // my term is equal to the other
        // i will check and vote

    }

    private void respondToAck(Request request) {
        if (RTerm.getTerm() < request.getTerm()) {
            // I'm dumb my term and state is bullshit
            // I become a follower and update my leader
            this.myState = RState.FOLLOWER;
            this.leader = request.getOrigin();
            this.timer.updateTimer(Duration.ofSeconds(App.FOLLOWER_TIMEOUT_IN_SECONDS));
        } else if (RTerm.getTerm() == request.getTerm()) {
            if (this.myState == RState.FOLLOWER) {
                if (this.leader == null) {
                    // I became a follower without a leader
                    // how is this possible
                    // I become a candidate (maybe I should crash)
                    this.becomeCandidate();
                } else if (this.leader.equals(request.getOrigin())) {
                    // all good
                    // I will be a good boy
                    this.timer.reset();
                } else {
                    // I believe a guy to be leader, while someone else claims to be a leader too
                    // what kind of magic is this
                    // my kingdom has only one leader
                    // I become a candidate now and look for future
                    this.becomeCandidate();
                }

            } else {
                // I'm leader
                // but someone else with same term as mine is claiming to be leader
                // I call his bullshit and become a CANDIDATE now
                this.becomeCandidate();
            }
        } else {
            if (this.myState == RState.FOLLOWER) {
                // I have higher term, and I'm a follower
                // but someone else with lower term is claiming to be leader
                // what ??
                // I call bullshit and become a CANDIDATE now
                this.becomeCandidate();
            } else if (this.myState == RState.LEADER) {
                // I have higher term, and I'm the leader
                // who is this guy saying he is the leader
                // I believe in myself
                log.info("[{}] Host {} claims to be leader, but his term [{}] is lower than mine [{}]", self, request.getOrigin(), request.getTerm(), RTerm.getTerm());
            } else {
                // I'm already a candidate with a higher term
                // someone with lower term than me is claiming to be a leader
                // I call his bullshit and increase my term
                this.becomeCandidate();
            }
        }
    }

    private void becomeCandidate() {
        this.myState = RState.CANDIDATE;
        this.leader = null;
        RTerm.incTerm();
        this.timer.updateTimer(Duration.ofSeconds(App.FOLLOWER_TIMEOUT_IN_SECONDS));
    }
}
