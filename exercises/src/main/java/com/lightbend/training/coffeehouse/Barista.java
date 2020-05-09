package com.lightbend.training.coffeehouse;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import scala.concurrent.duration.FiniteDuration;

@RequiredArgsConstructor
public class Barista extends AbstractLoggingActor {

    public static Props props(FiniteDuration prepareCoffeeDuration) {
        return Props.create(Barista.class, () -> new Barista(prepareCoffeeDuration));
    }

    private final FiniteDuration prepareCoffeeDuration;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PrepareCoffee.class, msg -> {
                    Utils.busy(prepareCoffeeDuration);
                    sender().tell(new CoffeePrepared(msg.getCoffee(), msg.getGuest()), self());
        }).build();
    }

    @Value
    public static class PrepareCoffee {
        Coffee coffee;
        ActorRef guest;
    }

    @Value
    public static class CoffeePrepared {
        Coffee coffee;
        ActorRef guest;
    }
}
