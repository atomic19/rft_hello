/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.rft.tone;

import com.rft.tone.config.Configuration;
import com.rft.tone.config.HostConfig;
import com.rft.tone.srv.LeaderFollower;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

@Log4j2
public class App {

    // used to test,
    // if > 0, leader will fail to send an ack and see how the leader is changed
    // if 0 nothing happens
    // if >= 100 always fails, please don't do that
    public static int FailAsALeaderPercent = 0;

    public static int LEADER_TIMEOUT_IN_SECONDS;

    public static int FOLLOWER_TIMEOUT_IN_SECONDS;

    // start from here
    private static final int MIN_TIMER_SEC = 2;

    // reach till here
    private static final int MAX_TIMER_SEC = 14;

    // keeps this amount of gap between each client
    // keep this easily divisible and the (MAX-MIN) * GAP >= clients
    private static final int GAP_IN_BETWEEN_SEC = 3;

    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("idx", true, "index of server from the servers file");
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("idx")) {
            int index = Integer.parseInt(cmd.getOptionValue("idx"));
            ArrayList<Integer> possibleValues = getPossibleValues();
            int timeoutInSeconds = possibleValues.get(index+1);
            LEADER_TIMEOUT_IN_SECONDS = possibleValues.get(0);
            FOLLOWER_TIMEOUT_IN_SECONDS = timeoutInSeconds;

            Configuration config = App.readConfig();
            HostConfig selfHostConfig = null;

            ArrayList<HostConfig> others = new ArrayList<>();

            for(int i : IntStream.range(0, config.getHosts().size()).toArray()) {
                if(i == index) {
                    selfHostConfig = config.getHosts().get(i);
                } else {
                    others.add(config.getHosts().get(i));
                }
            }

            assert selfHostConfig != null;

            log.info("***********");
            log.info("picked timeout in seconds: {} from possible: {}", timeoutInSeconds, Arrays.toString(possibleValues.toArray()));
            log.info("SELF: {}", selfHostConfig);
            log.info("OTHERS: {}", Arrays.toString(others.toArray()));
            log.info("***********");

            LeaderFollower.start(selfHostConfig, others, timeoutInSeconds);

        } else {
            throw new IllegalArgumentException("Need name and port as input parameter, use -idx index");
        }

        App.keepRunning();
    }

    private static Configuration readConfig() {
        Config config = ConfigFactory.load("hosts");
        return ConfigBeanFactory.create(config, Configuration.class);
    }

    private static ArrayList<Integer> getPossibleValues() {
        ArrayList<Integer> values = new ArrayList<Integer>();
        for (int i = MIN_TIMER_SEC; i <= MAX_TIMER_SEC; i += GAP_IN_BETWEEN_SEC) {
            values.add(i);
        }
        return values;
    }

    private static void keepRunning() throws InterruptedException {

        Object lock = new Object();
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (lock) {
            while (true) {
                lock.wait();
            }
        }

    }
}
