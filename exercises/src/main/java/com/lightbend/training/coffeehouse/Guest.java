package com.lightbend.training.coffeehouse;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Guest extends AbstractLoggingActor {

    public static Props props(ActorRef waiter, Coffee favoriteCoffee) {
        return Props.create(Guest.class, () -> new Guest(waiter, favoriteCoffee));
    }

    private final ActorRef waiter;
    private final Coffee favoriteCoffee;
    private int coffeeCount = 0;

    @Override
    public Receive createReceive() {
        //@formatter:off
        return receiveBuilder()
                .match(Waiter.CoffeeServed.class, msg -> {
                    coffeeCount += 1;
                    log().info("Enjoying my {} yummy {}!", coffeeCount, msg.getCoffee());
                }).matchEquals(CoffeeFinished.INSTANCE, msg -> waiter.tell(new Waiter.ServeCoffee(favoriteCoffee), self()))
                .build();
        //@formatter:on
    }

    public enum CoffeeFinished {
        INSTANCE
    }
}
