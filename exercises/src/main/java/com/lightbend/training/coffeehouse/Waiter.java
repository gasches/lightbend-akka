package com.lightbend.training.coffeehouse;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class Waiter extends AbstractLoggingActor {

    public static Props props(ActorRef coffeeHouse) {
        return Props.create(Waiter.class, () -> new Waiter(coffeeHouse));
    }

    private final ActorRef coffeeHouse;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ServeCoffee.class, msg ->
                        coffeeHouse.tell(new CoffeeHouse.ApproveCoffee(msg.getCoffee(), sender()), self()))
                .match(Barista.CoffeePrepared.class, msg ->
                        msg.getGuest().tell(new CoffeeServed(msg.getCoffee()), self()))
                .build();
    }

    @Value
    public static class ServeCoffee {
        Coffee coffee;
    }

    @Value
    public static class CoffeeServed {
        Coffee coffee;
    }
}
