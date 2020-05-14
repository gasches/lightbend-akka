package com.lightbend.training.coffeehouse;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import akka.actor.ActorIdentity;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Identify;
import akka.japi.pf.UnitPFBuilder;
import akka.testkit.EventFilter;
import akka.testkit.TestEvent;
import akka.testkit.TestProbe;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import scala.runtime.AbstractFunction0;

public abstract class BaseAkkaTest {

    protected final ActorSystem system;

    public BaseAkkaTest() {
        this.system = ActorSystem.create();
    }

    @BeforeClass
    public final void setup() {
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
    public final void shutdown() throws InterruptedException, TimeoutException {
        scala.concurrent.Await
                .ready(system.terminate(), scala.concurrent.duration.Duration.apply(20, TimeUnit.SECONDS));
    }

    protected final ActorRef expectActor(TestProbe probe, String path) {
        return expectActor(probe, path, FiniteDuration.apply(3L, TimeUnit.SECONDS));
    }

    protected final ActorRef expectActor(TestProbe probe, String path, FiniteDuration max) {
        return probe.within(max, new AbstractFunction0<>() {
            @Override
            public ActorRef apply() {
                AtomicReference<ActorRef> actor = new AtomicReference<>();
                probe.awaitAssert(() -> {
                    probe.system().actorSelection(path).tell(new Identify(path), probe.ref());
                    probe.expectMsgPF(Duration.create(100, TimeUnit.MILLISECONDS), "", new UnitPFBuilder<>()
                            .match(ActorIdentity.class, ident -> path.equals(ident.correlationId()),
                                    ident -> ident.getActorRef().ifPresent(actor::set)).build());
                    return null;
                }, Duration.Undefined(), Duration.Undefined());
                return actor.get();
            }
        });
    }
}
