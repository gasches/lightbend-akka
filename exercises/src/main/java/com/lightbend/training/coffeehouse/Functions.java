package com.lightbend.training.coffeehouse;

import scala.runtime.AbstractFunction0;

public final class Functions {

    private Functions() {
    }

    public static AbstractFunction0<Void> wrap(Runnable action) {
        return new AbstractFunction0<>() {
            @Override
            public Void apply() {
                action.run();
                return null;
            }
        };
    }
}
