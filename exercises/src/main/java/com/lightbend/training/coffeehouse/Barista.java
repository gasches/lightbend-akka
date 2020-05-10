package com.lightbend.training.coffeehouse;

import java.util.concurrent.ThreadLocalRandom;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import scala.concurrent.duration.FiniteDuration;

@RequiredArgsConstructor
public class Barista extends AbstractLoggingActor {

    public static Props props(FiniteDuration prepareCoffeeDuration, int accuracy) {
        return Props.create(Barista.class, () -> new Barista(prepareCoffeeDuration, accuracy));
    }

    private final FiniteDuration prepareCoffeeDuration;
    private final int accuracy;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PrepareCoffee.class, msg -> {
                    Utils.busy(prepareCoffeeDuration);
                    sender().tell(new CoffeePrepared(pickCoffee(msg.getCoffee()), msg.getGuest()), self());
        }).build();
    }

    private Coffee pickCoffee(Coffee coffee) {
        if (ThreadLocalRandom.current().nextInt(100) < accuracy) {
            return coffee;
        }
        return Coffee.anyOther(coffee);
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
