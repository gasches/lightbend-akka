package com.lightbend.training.coffeehouse;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Value;

public interface Terminal {

    interface Command {
    }

    @Value
    class Guest implements Command {
        private static final Pattern PATTERN =
                Pattern.compile("(?<count>\\d+\\s+)?(?:guest|g)(?<coffee>\\s+[AaMmCc])?(?<caffeineLimit>\\s+\\d+)?");

        int count;
        Coffee coffee;
        int caffeineLimit;
    }

    @Value
    class Status implements Command {
        private static final Pattern PATTERN = Pattern.compile("status|s");
        static final Status INSTANCE = new Status();
    }

    @Value
    class Quit implements Command {
        private static final Pattern PATTERN = Pattern.compile("quit|q");
        static final Quit INSTANCE = new Quit();
    }

    @Value
    class Unknown implements Command {
        String command;
    }

    static Command parseAsCommand(String command) {
        if (Status.PATTERN.matcher(command).matches()) {
            return Status.INSTANCE;
        } else if (Quit.PATTERN.matcher(command).matches()) {
            return Quit.INSTANCE;
        } else {
            Matcher matcher = Guest.PATTERN.matcher(command);
            if (!matcher.matches()) {
                return new Unknown(command);
            }
            int count = trimAndWrap(matcher.group("count")).map(Integer::valueOf).orElse(1);
            Coffee coffee = trimAndWrap(matcher.group("coffee")).map(Coffee::apply).orElse(Coffee.AKKACCINO);
            int caffeineLimit =
                    trimAndWrap(matcher.group("caffeineLimit")).map(Integer::valueOf).orElse(Integer.MAX_VALUE);
            return new Guest(count, coffee, caffeineLimit);
        }
    }

    static Optional<String> trimAndWrap(String str) {
        if (str == null) {
            return Optional.empty();
        }
        str = str.trim();
        if (str.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(str);
    }
}
