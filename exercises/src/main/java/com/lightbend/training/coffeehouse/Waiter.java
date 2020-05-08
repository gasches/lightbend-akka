package com.lightbend.training.coffeehouse;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import lombok.Value;

public class Waiter extends AbstractLoggingActor {

    public static Props props() {
        return Props.create(Waiter.class, Waiter::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ServeCoffee.class, msg -> sender().tell(new CoffeeServed(msg.getCoffee()), self()))
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
