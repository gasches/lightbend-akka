package com.lightbend.training.coffeehouse;

import org.testng.annotations.Test;

import akka.actor.ActorRef;
import akka.japi.JavaPartialFunction;
import akka.testkit.EventFilter;
import akka.testkit.TestProbe;

public class CoffeeHouseTest extends BaseAkkaTest {

    @Test(description = "Creating CoffeeHouse result in logging a status message at debug")
    public void testCreatingCoffeeHouse() {
        EventFilter.debug(null, null, "", ".*[Oo]pen.*", 1)
                .intercept(Functions.wrap(() -> system.actorOf(CoffeeHouse.props())), system);
    }

    @Test(description = "Sending a message to CoffeeHouse should result in sending a 'coffee brewing' message as response")
    public void testSendingMessage() {
        TestProbe sender = TestProbe.apply(system);
        ActorRef coffeeHouse = system.actorOf(CoffeeHouse.props());
        sender.send(coffeeHouse, "Brew Coffee");
        sender.expectMsgPF(scala.concurrent.duration.Duration.Undefined(), "", new JavaPartialFunction<>() {
            @Override
            public Object apply(Object message, boolean isCheck) {
                if (isCheck) {
                    return null;
                }
                if (message.toString().matches(".*[Cc]offee.*")) {
                    return null;
                }
                throw noMatch();
            }
        });
    }
}