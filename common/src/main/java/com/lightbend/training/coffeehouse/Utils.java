package com.lightbend.training.coffeehouse;

import java.math.BigDecimal;

import scala.concurrent.duration.Duration;

public final class Utils {

    private Utils() {
    }

    public static void busy(Duration duration) {
        pi(System.nanoTime() + duration.toNanos());
    }

    private static BigDecimal pi(long endNanos) {
        int n = 0;
        BigDecimal acc = new BigDecimal("0.0");
        while (System.nanoTime() < endNanos) {
            acc = acc.add(BigDecimal.valueOf(gregoryLeibnitz(n)));
            n += 1;
        }
        return acc;
    }

    private static double gregoryLeibnitz(long n) {
        return 4.0 * (1 - (n % 2) * 2) / (n * 2 + 1);
    }
}
