package com.lightbend.training.coffeehouse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import lombok.SneakyThrows;

public class CoffeeHouseApp implements Runnable {
    private static final Pattern OPT_PATTERN = Pattern.compile("(\\S+)=(\\S+)");

    public static void main(String[] args) {
        Map<String, String> opts = argsToOpts(args);
        applySystemProperties(opts);
        String name = opts.getOrDefault("name", "coffee-house");
        ActorSystem system = ActorSystem.create(name);
        new CoffeeHouseApp(system, CoffeeHouseApp::createCoffeeHouse).run();
    }

    static Map<String, String> argsToOpts(String[] args) {
        return Arrays.stream(args).map(OPT_PATTERN::matcher).filter(Matcher::matches)
                .collect(Collectors.toMap(m -> m.group(1), m -> m.group(2)));
    }

    static void applySystemProperties(Map<String, String> opts) {
        opts.forEach((k, v) -> {
            if (k.startsWith("-D")) {
                System.setProperty(k.substring(2), v);
            }
        });
    }

    static ActorRef createCoffeeHouse(ActorSystem system) {
        return system.actorOf(CoffeeHouse.props(), "coffee-house");
    }

    private final ActorSystem system;
    private final LoggingAdapter log;
    private final ActorRef coffeeHouse;

    public CoffeeHouseApp(ActorSystem system, Function<ActorSystem, ActorRef> coffeeHouseCreator) {
        this.system = system;
        this.log = Logging.getLogger(system, getClass().getName());
        this.coffeeHouse = coffeeHouseCreator.apply(system);
    }

    @Override
    @SneakyThrows
    public void run() {
        log.warning("{} running\nEnter commands into the terminal: [e.g. `q` or `quit`]", getClass().getSimpleName());
        commandLoop();
        scala.concurrent.Await.ready(system.whenTerminated(), scala.concurrent.duration.Duration.Inf());
    }

    private void commandLoop() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = reader.readLine();
            Terminal.Command command = Terminal.parseAsCommand(line);
            if (command instanceof Terminal.Guest) {
                Terminal.Guest guestCmd = (Terminal.Guest) command;
                createGuest(guestCmd.getCount(), guestCmd.getCoffee(), guestCmd.getCaffeineLimit());
            } else if (command instanceof Terminal.Status) {
                status();
            } else if (command instanceof Terminal.Quit) {
                system.terminate();
                break;
            } else {
                log.warning("Unknown command {}!", command);
            }
        }
    }

    protected void createGuest(int count, Coffee coffee, int caffeineLimit) {
        for (int i = 0; i < count; i++) {
            coffeeHouse.tell(new CoffeeHouse.CreateGuest(coffee), coffeeHouse);
        }
    }

    protected void status() {
    }
}
