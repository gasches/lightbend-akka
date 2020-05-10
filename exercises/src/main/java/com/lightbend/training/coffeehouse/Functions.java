package com.lightbend.training.coffeehouse;

import java.util.function.Supplier;

import scala.runtime.AbstractFunction0;

public final class Functions {

    private Functions() {
    }

    public static <T> AbstractFunction0<T> wrap(Supplier<T> supplier) {
        return new AbstractFunction0<>() {
            @Override
            public T apply() {
                return supplier.get();
            }
        };
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
