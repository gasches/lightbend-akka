package com.lightbend.training.coffeehouse;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.FiniteDuration;

public class Guest extends AbstractActorWithTimers {

    public static Props props(ActorRef waiter, Coffee favoriteCoffee, FiniteDuration finishCoffeeDuration,
            int caffeineLimit) {
        return Props.create(Guest.class, () -> new Guest(waiter, favoriteCoffee, finishCoffeeDuration, caffeineLimit));
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private final ActorRef waiter;
    private final Coffee favoriteCoffee;
    private final FiniteDuration finishCoffeeDuration;
    private final int caffeineLimit;

    private int coffeeCount = 0;

    public Guest(ActorRef waiter, Coffee favoriteCoffee, FiniteDuration finishCoffeeDuration, int caffeineLimit) {
        this.waiter = waiter;
        this.favoriteCoffee = favoriteCoffee;
        this.finishCoffeeDuration = finishCoffeeDuration;
        this.caffeineLimit = caffeineLimit;

        orderFavoriteCoffee();
    }

    @Override
    public Receive createReceive() {
        //@formatter:off
        return receiveBuilder()
                .match(Waiter.CoffeeServed.class, msg -> msg.getCoffee() == favoriteCoffee, msg -> {
                    coffeeCount += 1;
                    log.info("Enjoying my {} yummy {}!", coffeeCount, msg.getCoffee());
                    timers().startSingleTimer("coffee-finished", CoffeeFinished.INSTANCE, finishCoffeeDuration);
                }).match(Waiter.CoffeeServed.class, msg -> {
                    log.info("Expected a {}, but got a {}!", favoriteCoffee, msg.getCoffee());
                    waiter.tell(new Waiter.Complaint(favoriteCoffee), self());
                }).matchEquals(CoffeeFinished.INSTANCE, msg -> coffeeCount >= caffeineLimit, msg -> {
                    throw new CaffeineException();
                }).matchEquals(CoffeeFinished.INSTANCE, msg -> orderFavoriteCoffee())
                .build();
        //@formatter:on
    }

    @Override
    public void postStop() throws Exception {
        log.info("Goodbye!");
        super.postStop();
    }

    private void orderFavoriteCoffee() {
        waiter.tell(new Waiter.ServeCoffee(favoriteCoffee), self());
    }

    public enum CoffeeFinished {
        INSTANCE
    }

    public static class CaffeineException extends IllegalStateException {
        public CaffeineException() {
            super("Too much caffeine!");
        }
    }
}
