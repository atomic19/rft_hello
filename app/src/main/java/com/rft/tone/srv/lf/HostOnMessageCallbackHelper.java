package com.rft.tone.srv.lf;

import com.rft.tone.App;
import com.rft.tone.rstates.RState;
import com.rft.tone.rstates.RTerm;
import com.rft.tone.srv.Host;
import com.rft.tone.srv.Request;
import com.rft.tone.srv.interfaces.ClientConnectionsHandler;
import com.rft.tone.srv.interfaces.OnMessageCallback;
import com.rft.tone.srv.interfaces.RTimer;
import com.rft.tone.srv.interfaces.SendMessages;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.util.Random;

@Log4j2
@Data
public class HostOnMessageCallbackHelper implements OnMessageCallback {

    private volatile RState myState;
    private volatile Host leader;
    private final Host self;
    private final RTimer timer;
    private final VoteManager voteManager;
    private final Object stateLock = new Object();
    private Random random = new Random();

    private ClientConnectionsHandler clientConnectionsHandler;
    private SendMessages sendMessages;

    public HostOnMessageCallbackHelper(Host self, RTimer timer) {
        this.self = self;
        this.timer = timer;
        this.myState = RState.CANDIDATE;
        this.voteManager = new VoteManager();
    }

    @Override
    public void onMessage(Request request) {
        synchronized (this.stateLock) {
            log.info("START *************************************** {}", request);
            log.info("[{}][Term:{}] a message has arrived: {}", self, RTerm.getTerm(), request);
            switch (request.getRequestType()) {
                case ACK:
                    this.respondToAck(request);
                    break;
                case VOTE_FOR_ME:
                    this.respondToVoteForMe(request);
                    break;
                case VOTE_RESPONSE:
                    this.respondToVoteResponse(request);
                    break;
                default:
                    log.info("{} Unknown state: {}", self, request);
            }
            log.info("END *************************************** {}", request);
        }
    }

    public void handleOnTime() {
        synchronized (this.stateLock) {
            log.info("*************\n[{}] ON TIME: MyState: {}", this.self, this.myState);
            if(this.myState == RState.LEADER) {
                if(App.FailAsALeaderPercent == 0 || this.random.nextInt(100) > App.FailAsALeaderPercent) {
                    Request request = new Request(Request.RequestType.ACK, RTerm.getTerm(), this.self);
                    this.sendMessages.sendMessage(request);
                } else {
                    log.info("////////// I SKIPPED AS A LEADER FAILED ON PURPOSE WHY ARE YOU FORCING ME TO DO THIS");
                }
            } else if(this.myState == RState.FOLLOWER) {
                // I'm a follower, hm, I should increase my term and become a candidate
                this.myState = RState.CANDIDATE;
                RTerm.incTerm();
            }

            if (this.myState == RState.CANDIDATE) {
                // I'm a candidate, it's my obligation to request for a vote now
                // I should reset my state only if the vote terms mismatch
                // VoteManager will do that for me
                this.voteManager.reset(RTerm.getTerm(), this.clientConnectionsHandler.getCurrentHosts());
                Request req  = new Request(Request.RequestType.VOTE_FOR_ME, RTerm.getTerm(), this.self);
                this.sendMessages.sendMessage(req);
            }
        }
    }

    public void respondToVoteResponse(Request request) {
        if(request.getTerm() > RTerm.getTerm()) {
            // I got response for a vote with term greater than mine
            // but how is this magic possible
            log.error("[{}] Black from host for VoteResponse: {}", this.self, request);
        } else if(request.getTerm() < RTerm.getTerm()) {
            // I got response for a vote with term less than mine
            // this is old, I'm going to ignore this vote and stay as is
            log.info("[{}] got an old vote with VoteResponse: {}", this.self, request);
        } else {
            this.voteManager.getVoteResponse().put(request.getOrigin(), request.getVoteResponse());
            if(this.voteManager.amILeader(this.self)) {
                // I became the king
                // I will update my timer
                // and send an ACK immediately to claim my LEADER-ship;
                this.myState = RState.LEADER;
                Request req = new Request(Request.RequestType.ACK, RTerm.getTerm(), this.self);
                this.sendMessages.sendMessage(req);
                this.timer.updateTimer(Duration.ofSeconds(App.LEADER_TIMEOUT_IN_SECONDS));
                log.info("*********************************************************");
                log.info("LEADER ELECTED HERE: {} MyTerm: {}", self, RTerm.getTerm());
                log.info("*********************************************************");
            } else {
                log.info("[{}] MyTerm: {} event after getting a vote, i'm not leader yet", self, RTerm.getTerm());
            }
        }
    }

    private void respondToVoteForMe(Request request) {
        if (RTerm.getTerm() > request.getTerm()) {
            // I have higher term
            // I will not vote and stay as is and ignore this person
            log.info("[{}] a host {} is asking for my vote, but i refuse to give it as i'm ", this.self, request.getOrigin());
        } else  if (RTerm.getTerm() == request.getTerm()) {
            // Our terms are equal and someone is asking for a vote
            // I should first check if I'm the leader,
            if(myState == RState.LEADER) {
                // If I'm the leader I will ignore this person
                log.info("[{}] VoteForMe Case: i'm leader with same term, so ignore him", self);
            } else if (myState == RState.FOLLOWER) {
                // If I'm a follower, then I will new increase my term and become a candidate and not vote
                log.info("[{}] VoteForMe Case: I'm a follower with the same term and I have a leader, " +
                        "but someone else is asking for votes, " +
                        "i will increase my term and try to become a leader myself", self);
                this.becomeCandidate();
            } else {
                // If I'm a Candidate, then will I vote as per the law
                log.info("[{}] I'm a candidate without a leader i will continue to vote", self);
                this.voteAsNeeded(request);
            }
        } else if (RTerm.getTerm() < request.getTerm()) {
            // my term is lower, updating my term the requested on and voting
            updateTermToThis(request.getTerm());
            // I will invalidate the current leader
            this.leader = null;
            // I myself will also become a candidate
            this.myState = RState.CANDIDATE;
            // and also vote in this term
            this.voteAsNeeded(request);
        }
    }

    private void voteAsNeeded(Request request) {
        // my term is equal to the other
        // I'm ready to vote now
        // will check and vote
        this.voteManager.reset(RTerm.getTerm(), this.clientConnectionsHandler.getCurrentHosts());

        Host votedFor = null;
        if(this.voteManager.getVotedFor().get(request.getOrigin()) == null) {
            this.voteManager.getVotedFor().put(request.getOrigin(), request.getOrigin());
            // send message to this host
            votedFor = request.getOrigin();
        } else {
            votedFor = this.voteManager.getVotedFor().get(request.getOrigin());
        }

        Request voteResponse = new Request(Request.RequestType.VOTE_RESPONSE, RTerm.getTerm(), this.self);
        voteResponse.setVoteResponse(votedFor);
        this.clientConnectionsHandler.sendMessageToHost(request.getOrigin(), voteResponse);

        log.info("[{}] MyTerm: {} VoteResponseRequest: {}", self, RTerm.getTerm(), voteResponse);
        // I have just voted, so I will reset my timer and wait for the new Leader if anyone comes
        this.timer.reset();
    }

    private void respondToAck(Request request) {
        if (RTerm.getTerm() < request.getTerm()) {
            // I'm dumb my term and state is bullshit
            // I become a follower and update my leader
            this.becomeFollower(request.getOrigin());
            // I also update my term to the leaders
            this.updateTermToThis(request.getTerm());
            log.info("[{}] case 1", self);
        } else if (RTerm.getTerm() == request.getTerm()) {
            if (this.myState == RState.FOLLOWER) {
                if (this.leader == null) {
                    // I became a follower without a leader
                    // how is this possible
                    // I become a candidate (maybe I should crash)
                    log.info("[{}] case 2", self);
                    this.becomeCandidate();
                } else if (this.leader.equals(request.getOrigin())) {
                    // all good
                    // I will be a good boy
                    this.timer.reset();
                    log.info("[{}] Default good Case", self);
                } else {
                    // I believe a guy to be leader, while someone else claims to be a leader too
                    // what kind of magic is this
                    // my kingdom has only one leader
                    // I become a candidate now and look for future
                    this.becomeCandidate();
                    log.info("[{}] case 3", self);
                }

            } else if(this.myState == RState.CANDIDATE) {
              // Someone says they are the Leader
              // we both have same terms
              // because he claimed to be leader, I will accept him as my leader
              log.info("[{}] i became a follower because of this request: {}", self, request);
              this.becomeFollower(request.getOrigin());
            } else {
                // I'm leader
                // but someone else with same term as mine is claiming to be leader
                // I call his bullshit and become a CANDIDATE now
                this.becomeCandidate();
                log.info("[{}] case 4", self);
            }
        } else {
            if (this.myState == RState.FOLLOWER) {
                // I have higher term, and I'm a follower
                // but someone else with lower term is claiming to be leader
                // what ??
                // I call bullshit and become a CANDIDATE now
                this.becomeCandidate();
                log.info("[{}] case 5", self);
            } else if (this.myState == RState.LEADER) {
                // I have higher term, and I'm the leader
                // who is this guy saying he is the leader
                // I believe in myself
                log.info("[{}] Host {} claims to be leader, but his term [{}] is lower than mine [{}]", self, request.getOrigin(), request.getTerm(), RTerm.getTerm());
            } else {
                // I'm already a candidate with a higher term
                // someone with lower term than me is claiming to be a leader
                // I ignore him
                log.info("[{}] case 6", self);
            }
        }
    }

    private void becomeCandidate() {
        this.myState = RState.CANDIDATE;
        this.leader = null;
        RTerm.incTerm();
        this.timer.updateTimer(Duration.ofSeconds(App.FOLLOWER_TIMEOUT_IN_SECONDS));
    }

    private void updateTermToThis(long otherTerm) {
        long diff = otherTerm - RTerm.getTerm();
        while (diff > 0) {
            RTerm.incTerm();
            diff -= 1;
        }
    }

    private void becomeFollower(Host leader) {
        this.myState = RState.FOLLOWER;
        this.leader = leader;
        this.timer.updateTimer(Duration.ofSeconds(App.FOLLOWER_TIMEOUT_IN_SECONDS));
    }
}
