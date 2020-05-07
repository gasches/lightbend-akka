package com.lightbend.training.coffeehouse;

import java.util.function.Supplier;

import scala.runtime.AbstractFunction0;

public final class Functions {

    private Functions() {
    }

    public static <T> AbstractFunction0<T> wrap(Supplier<T> supplier) {
        return new AbstractFunction0<T>() {
            @Override
            public T apply() {
                return supplier.get();
            }
        };
    }
}
