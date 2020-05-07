package com.lightbend.training.coffeehouse;

import org.testng.annotations.Test;

import akka.actor.ActorRef;
import akka.testkit.EventFilter;

public class CoffeeHouseTest extends BaseAkkaTest {

    @Test(description = "Creating CoffeeHouse result in logging a status message at debug")
    public void testCreatingCoffeeHouse() {
        EventFilter.debug(null, null, "", ".*[Oo]pen.*", 1)
                .intercept(Functions.wrap(() -> system.actorOf(CoffeeHouse.props())), system);
    }

    @Test(description = "Sending a message to CoffeeHouse should result in logging a 'coffee brewing' message at info")
    public void testSendingMessage() {
        ActorRef coffeeHouse = system.actorOf(CoffeeHouse.props());
        EventFilter.info(null, coffeeHouse.path().toString(), "", ".*[Cc]offee.*", 1)
                .intercept(Functions.wrap(() -> {
                    coffeeHouse.tell("Brew Coffee", ActorRef.noSender());
                    return null;
                }), system);
    }
}