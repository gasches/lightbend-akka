package com.lightbend.training.coffeehouse;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import lombok.Value;

public class CoffeeHouse extends AbstractLoggingActor {

    public static Props props() {
        return Props.create(CoffeeHouse.class, CoffeeHouse::new);
    }

    private final ActorRef waiter;

    public CoffeeHouse() {
        log().debug("CoffeeHouse Open");
        this.waiter = createWaiter();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateGuest.class, msg -> createGuest(msg.getFavoriteCoffee()))
                .build();
    }

    protected ActorRef createWaiter() {
        return context().actorOf(Waiter.props(), "waiter");
    }

    protected ActorRef createGuest(Coffee favoriteCoffee) {
        return context().actorOf(Guest.props(waiter, favoriteCoffee));
    }

    @Value
    public static class CreateGuest {
        Coffee favoriteCoffee;
    }
}
