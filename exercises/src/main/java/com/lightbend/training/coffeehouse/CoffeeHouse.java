package com.lightbend.training.coffeehouse;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class CoffeeHouse extends AbstractLoggingActor {

    public static Props props() {
        return Props.create(CoffeeHouse.class, CoffeeHouse::new);
    }

    public CoffeeHouse() {
        log().debug("CoffeeHouse Open");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals(CreateGuest.INSTANCE, msg -> createGuest())
                .build();
    }

    protected ActorRef createGuest() {
        return context().actorOf(Guest.props());
    }

    public enum CreateGuest {
        INSTANCE
    }
}
