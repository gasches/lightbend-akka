package com.lightbend.training.coffeehouse;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import akka.actor.ActorSystem;
import akka.testkit.EventFilter;
import akka.testkit.TestEvent;

public abstract class BaseAkkaTest {

    protected final ActorSystem system;

    public BaseAkkaTest() {
        this.system = ActorSystem.create();
    }

    @BeforeClass
    public void setup() {
        system.eventStream()
                .publish(new TestEvent.Mute(List.of(EventFilter.debug(null, null, "", null, Integer.MAX_VALUE))));
        system.eventStream()
                .publish(new TestEvent.Mute(List.of(EventFilter.info(null, null, "", null, Integer.MAX_VALUE))));
        system.eventStream()
                .publish(new TestEvent.Mute(List.of(EventFilter.warning(null, null, "", null, Integer.MAX_VALUE))));
        system.eventStream()
                .publish(new TestEvent.Mute(List.of(EventFilter.error(null, null, "", null, Integer.MAX_VALUE))));
    }

    @AfterClass
    public void shutdown() throws InterruptedException, TimeoutException {
        scala.concurrent.Await
                .ready(system.terminate(), scala.concurrent.duration.Duration.apply(20, TimeUnit.SECONDS));
    }
}
