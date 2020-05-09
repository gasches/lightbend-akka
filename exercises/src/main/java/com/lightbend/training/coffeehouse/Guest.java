package com.lightbend.training.coffeehouse;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.FiniteDuration;

public class Guest extends AbstractActorWithTimers {

    public static Props props(ActorRef waiter, Coffee favoriteCoffee, FiniteDuration finishCoffeeDuration) {
        return Props.create(Guest.class, () -> new Guest(waiter, favoriteCoffee, finishCoffeeDuration));
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private final ActorRef waiter;
    private final Coffee favoriteCoffee;
    private final FiniteDuration finishCoffeeDuration;

    private int coffeeCount = 0;

    public Guest(ActorRef waiter, Coffee favoriteCoffee, FiniteDuration finishCoffeeDuration) {
        this.waiter = waiter;
        this.favoriteCoffee = favoriteCoffee;
        this.finishCoffeeDuration = finishCoffeeDuration;

        orderFavoriteCoffee();
    }

    @Override
    public Receive createReceive() {
        //@formatter:off
        return receiveBuilder()
                .match(Waiter.CoffeeServed.class, msg -> {
                    coffeeCount += 1;
                    log.info("Enjoying my {} yummy {}!", coffeeCount, msg.getCoffee());
                    timers().startSingleTimer("coffee-finished", CoffeeFinished.INSTANCE, finishCoffeeDuration);
                }).matchEquals(CoffeeFinished.INSTANCE, msg -> orderFavoriteCoffee())
                .build();
        //@formatter:on
    }

    private void orderFavoriteCoffee() {
        waiter.tell(new Waiter.ServeCoffee(favoriteCoffee), self());
    }

    public enum CoffeeFinished {
        INSTANCE
    }
}
