package com.lightbend.training.coffeehouse;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

public class Guest extends AbstractLoggingActor {

    public static Props props() {
        return Props.create(Guest.class, Guest::new);
    }

    @Override
    public Receive createReceive() {
        return emptyBehavior();
    }
}
