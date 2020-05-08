package com.lightbend.training.coffeehouse;

import java.util.Map;

import org.testng.annotations.Test;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.japi.JavaPartialFunction;
import akka.testkit.EventFilter;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import scala.PartialFunction;
import scala.reflect.ClassTag;

import static org.testng.Assert.assertEquals;

public class CoffeeHouseAppTest extends BaseAkkaTest {

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
        new CoffeeHouseApp(system, CoffeeHouseApp::createCoffeeHouse);
        expectActor(new TestProbe(system), "/user/coffee-house");
    }

    @Test(description = "Creating CoffeeHouseApp result in sending a message to CoffeeHouse")
    public void testSendingMessageOnActorCreation() {
        TestProbe coffeeHouse = new TestProbe(system);
        new CoffeeHouseApp(system, s -> coffeeHouse.ref());
        coffeeHouse.expectMsgType(ClassTag.Any());
    }

    @Test(description = "Creating CoffeeHouseApp result in logging CoffeeHouse's response at info")
    public void testCoffeeHouseResponse() {
        PartialFunction<Logging.LogEvent, Object> testFunction = new JavaPartialFunction<>() {
            @Override
            public Object apply(Logging.LogEvent event, boolean isCheck) {
                if (event instanceof Logging.Info && "response".equals(event.message())) {
                    if (isCheck) {
                        return null;
                    }
                    return event.logSource().contains("$");
                } else {
                    throw noMatch();
                }
            }
        };
        EventFilter.custom(testFunction, 1).intercept(Functions.wrap(() -> new CoffeeHouseApp(system,
                s -> TestActorRef.create(s, Props.create(AnonymousActor.class, AnonymousActor::new)))), system);
    }

    static class AnonymousActor extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder().matchAny(msg -> sender().tell("response", self())).build();
        }
    }
}