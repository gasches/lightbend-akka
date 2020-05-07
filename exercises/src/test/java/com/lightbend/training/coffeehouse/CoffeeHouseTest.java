package com.lightbend.training.coffeehouse;

import org.testng.annotations.Test;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.EventFilter;

public class CoffeeHouseTest extends BaseAkkaTest {

    @Test(description = "Sending a message to CoffeeHouse should result in logging a 'coffee brewing' message at info")
    public void testCatchAllMessages() {
        ActorRef coffeeHouse = system.actorOf(Props.create(CoffeeHouse.class, CoffeeHouse::new));
        EventFilter.info(null, coffeeHouse.path().toString(), "", ".*[Cc]offee.*", 1)
                .intercept(Functions.wrap(() -> {
                    coffeeHouse.tell("Brew Coffee", ActorRef.noSender());
                    return null;
                }), system);
    }
}