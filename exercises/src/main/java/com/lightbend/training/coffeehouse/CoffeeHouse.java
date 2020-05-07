package com.lightbend.training.coffeehouse;

import akka.actor.AbstractLoggingActor;

public class CoffeeHouse extends AbstractLoggingActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchAny(msg -> log().info("Coffee Brewing"))
                .build();
    }
}
