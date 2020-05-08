package com.lightbend.training.coffeehouse;

import org.testng.annotations.Test;

import akka.actor.ActorRef;
import akka.testkit.EventFilter;
import akka.testkit.TestProbe;

public class CoffeeHouseTest extends BaseAkkaTest {

    @Test(description = "Creating CoffeeHouse result in logging a status message at debug")
    public void testCreatingCoffeeHouse() {
        EventFilter.debug(null, null, "", ".*[Oo]pen.*", 1)
                .intercept(Functions.wrap(() -> system.actorOf(CoffeeHouse.props())), system);
    }

    @Test(description = "Sending CreateGuest to CoffeeHouse should result in creating a Guest")
    public void testCreateGuest() {
        ActorRef coffeeHouse = system.actorOf(CoffeeHouse.props());
        coffeeHouse.tell(CoffeeHouse.CreateGuest.INSTANCE, coffeeHouse);
        expectActor(TestProbe.apply(system), "/user/create-guest/$*");
    }
}