package com.lightbend.training.coffeehouse;

import java.util.concurrent.ThreadLocalRandom;

import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Props;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import scala.concurrent.duration.FiniteDuration;

@RequiredArgsConstructor
public class Barista extends AbstractActorWithStash {

    public static Props props(FiniteDuration prepareCoffeeDuration, int accuracy) {
        return Props.create(Barista.class, () -> new Barista(prepareCoffeeDuration, accuracy));
    }

    private final FiniteDuration prepareCoffeeDuration;
    private final int accuracy;

    @Override
    public Receive createReceive() {
        return ready();
    }

    private Receive ready() {
        return receiveBuilder()
                .match(PrepareCoffee.class, msg -> {
                    context().system().scheduler().scheduleOnce(prepareCoffeeDuration, self(),
                            new CoffeePrepared(pickCoffee(msg.getCoffee()), msg.getGuest()), context().dispatcher(),
                            self());
                    context().become(busy(sender()).onMessage());
                }).build();
    }

    private Receive busy(ActorRef waiter) {
        return receiveBuilder()
                .match(CoffeePrepared.class, msg -> {
                    waiter.tell(msg, self());
                    unstashAll();
                    context().become(ready().onMessage());
                }).matchAny(msg -> stash())
                .build();
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
