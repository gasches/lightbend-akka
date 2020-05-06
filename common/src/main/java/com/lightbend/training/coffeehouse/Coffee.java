package com.lightbend.training.coffeehouse;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public enum Coffee {

    AKKACCINO,
    MOCHA_PLAY,
    CAFFE_SCALA;

    public static EnumSet<Coffee> beverages() {
        return EnumSet.allOf(Coffee.class);
    }

    public static Coffee apply(String code) {
        switch (code.toLowerCase()) {
            case "a":
                return AKKACCINO;
            case "m":
                return MOCHA_PLAY;
            case "c":
                return CAFFE_SCALA;
            default:
                throw new IllegalArgumentException("Unknown beverage code " + code);
        }
    }

    public static Coffee anyOther(Coffee beverage) {
        List<Coffee> beverages = beverages().stream().filter(c -> c != beverage).collect(Collectors.toList());
        return beverages.get(ThreadLocalRandom.current().nextInt(beverages.size()));
    }
}
