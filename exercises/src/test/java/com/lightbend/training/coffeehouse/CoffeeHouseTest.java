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

    @Test(description = "Creating CoffeeHouse result in creating a child actor with the name 'barista'")
    public void testBaristaCreated() {
        system.actorOf(CoffeeHouse.props(), "create-barista");
        expectActor(TestProbe.apply(system), "/user/create-barista/barista");
    }

    @Test(description = "Creating CoffeeHouse result in creating a child actor with the name 'waiter'")
    public void testWaiterCreated() {
        system.actorOf(CoffeeHouse.props(), "create-waiter");
        expectActor(TestProbe.apply(system), "/user/create-waiter/waiter");
    }

    @Test(description = "Sending CreateGuest to CoffeeHouse should result in creating a Guest")
    public void testCreateGuest() {
        ActorRef coffeeHouse = system.actorOf(CoffeeHouse.props(), "create-guest");
        coffeeHouse.tell(new CoffeeHouse.CreateGuest(Coffee.AKKACCINO), coffeeHouse);
        expectActor(TestProbe.apply(system), "/user/create-guest/$*");
    }
}