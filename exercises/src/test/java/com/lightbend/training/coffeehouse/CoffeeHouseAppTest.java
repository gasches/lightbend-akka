package com.lightbend.training.coffeehouse;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import akka.actor.AbstractActor;
import akka.actor.Actor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.EventFilter;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import akka.util.Timeout;
import scala.collection.immutable.Seq;
import scala.concurrent.duration.FiniteDuration;

import static org.testng.Assert.assertEquals;

public class CoffeeHouseAppTest extends BaseAkkaTest {

    private final Timeout statusTimeout = Timeout.apply(FiniteDuration.apply(100, TimeUnit.MILLISECONDS));

    @Test(description = "Calling argsToOpts should return the correct opts for the given args")
    public void testArgsToOpts() {
        Map<String, String> opts = CoffeeHouseApp.argsToOpts(new String[] {"a=1", "b", "-Dc=2"});
        assertEquals(opts, Map.of("a", "1", "-Dc", "2"));
    }

    @Test(description = "Calling applySystemProperties should apply the system properties for the given opts")
    public void testApplySystemProperties() {
        System.setProperty("c", "");
        CoffeeHouseApp.applySystemProperties(Map.of("a", "1", "-Dc", "2"));
        assertEquals(System.getProperty("c"), "2");
    }

    @Test(description = "Creating CoffeeHouseApp result in creating a top-level actor named 'coffee-house'")
    public void testActorCreation() {
        new CoffeeHouseApp(system, statusTimeout, CoffeeHouseApp::createCoffeeHouse);
        expectActor(TestProbe.apply(system), "/user/coffee-house");
    }

    @Test(description = "Calling createGuest should result in sending CreateGuest to CoffeeHouse count number of times")
    public void testCreateGuest() {
        TestProbe probe = TestProbe.apply(system);
        CoffeeHouseApp coffeeHouseApp = new CoffeeHouseApp(system, statusTimeout, s -> probe.ref());
        coffeeHouseApp.createGuest(2, Coffee.AKKACCINO, Integer.MAX_VALUE);
        assertEquals(probe.receiveN(2),
                Seq.fill(2, () -> new CoffeeHouse.CreateGuest(Coffee.AKKACCINO, Integer.MAX_VALUE)));
    }

    @Test(description = "Calling getStatus should result in logging the AskTimeoutException at error for CoffeeHouse not responding")
    public void testGetStatusResultsInLoggingAskTimeoutException() {
        CoffeeHouseApp coffeeHouseApp = new CoffeeHouseApp(system, statusTimeout, ActorSystem::deadLetters);
        EventFilter.error(null, null, "", ".*AskTimeoutException.*", Integer.MAX_VALUE)
                .intercept(Functions.wrap(coffeeHouseApp::status), system);
    }

    @Test(description = "Calling getStatus should result in logging the status at info")
    public void testGetStatusResultsInLoggingStatusInfo() {
        CoffeeHouseApp coffeeHouseApp = new CoffeeHouseApp(system, statusTimeout,
                s -> s.actorOf(Props.create(CoffeeHouseStub.class, CoffeeHouseStub::new)));
        EventFilter.info(null, null, "", ".*42.*", Integer.MAX_VALUE)
                .intercept(Functions.wrap(coffeeHouseApp::status), system);
    }

    @Test(description = "Sending GetStatus to CoffeeHouse should result in a Status response")
    public void testGetStatusResultsInStatusResponse() {
        TestProbe sender = TestProbe.apply(system);
        TestActorRef<Actor> coffeeHouse = TestActorRef
                .create(system, Props.create(CoffeeHouse.class, () -> new CoffeeHouse(Integer.MAX_VALUE)),
                        "get-status");
        coffeeHouse.tell(CoffeeHouse.GetStatus.INSTANCE, sender.ref());
        sender.expectMsg(new CoffeeHouse.Status(0));
    }

    private static class CoffeeHouseStub extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchEquals(CoffeeHouse.GetStatus.INSTANCE, msg ->
                            sender().tell(new CoffeeHouse.Status(42), self())
                    ).build();
        }
    }
}