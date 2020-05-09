package com.lightbend.training.coffeehouse;

import java.util.concurrent.TimeUnit;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import lombok.Value;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public class CoffeeHouse extends AbstractLoggingActor {

    public static Props props() {
        return Props.create(CoffeeHouse.class, CoffeeHouse::new);
    }

    private final ActorRef waiter;
    private final FiniteDuration guestFinishCoffeeDuration;

    public CoffeeHouse() {
        log().debug("CoffeeHouse Open");
        this.guestFinishCoffeeDuration = Duration.create(context().system().settings().config()
                        .getDuration("coffee-house.guest.finish-coffee-duration", TimeUnit.MILLISECONDS),
                TimeUnit.MILLISECONDS);
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
        return context().actorOf(Guest.props(waiter, favoriteCoffee, guestFinishCoffeeDuration));
    }

    @Value
    public static class CreateGuest {
        Coffee favoriteCoffee;
    }
}
