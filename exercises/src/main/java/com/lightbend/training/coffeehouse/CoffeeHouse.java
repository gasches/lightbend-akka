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

    private final ActorRef barista;
    private final ActorRef waiter;
    private final FiniteDuration baristaPrepareCoffeeDuration;
    private final FiniteDuration guestFinishCoffeeDuration;

    public CoffeeHouse() {
        this.baristaPrepareCoffeeDuration = Duration.create(context().system().settings().config()
                        .getDuration("coffee-house.barista.prepare-coffee-duration", TimeUnit.MILLISECONDS),
                TimeUnit.MILLISECONDS);
        this.guestFinishCoffeeDuration = Duration.create(context().system().settings().config()
                        .getDuration("coffee-house.guest.finish-coffee-duration", TimeUnit.MILLISECONDS),
                TimeUnit.MILLISECONDS);
        this.barista = createBarista();
        this.waiter = createWaiter();
        log().debug("CoffeeHouse Open");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateGuest.class, msg -> createGuest(msg.getFavoriteCoffee()))
                .build();
    }

    protected ActorRef createBarista() {
        return context().actorOf(Barista.props(baristaPrepareCoffeeDuration), "barista");
    }

    protected ActorRef createWaiter() {
        return context().actorOf(Waiter.props(barista), "waiter");
    }

    protected ActorRef createGuest(Coffee favoriteCoffee) {
        return context().actorOf(Guest.props(waiter, favoriteCoffee, guestFinishCoffeeDuration));
    }

    @Value
    public static class CreateGuest {
        Coffee favoriteCoffee;
    }
}
