package com.lightbend.training.coffeehouse;

import org.testng.annotations.Test;

import akka.actor.ActorRef;
import akka.testkit.TestProbe;

public class WaterTest extends BaseAkkaTest {

    @Test(description = "Sending ServeCoffee to Waiter should result in sending ApproveCoffee to CoffeeHouse")
    public void testResponseCoffeeServedOnServeCoffee() {
        TestProbe coffeeHouse = TestProbe.apply(system);
        TestProbe guest = TestProbe.apply(system);
        ActorRef waiter = system.actorOf(Waiter.props(coffeeHouse.ref()));
        waiter.tell(new Waiter.ServeCoffee(Coffee.AKKACCINO), guest.ref());
        coffeeHouse.expectMsg(new CoffeeHouse.ApproveCoffee(Coffee.AKKACCINO, guest.ref()));
    }
}
