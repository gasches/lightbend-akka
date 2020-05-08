package com.lightbend.training.coffeehouse;

import org.testng.annotations.Test;

import akka.actor.ActorRef;
import akka.testkit.TestProbe;

public class WaterTest extends BaseAkkaTest {

    @Test(description = "Sending ServeCoffee to Waiter should result in sending a CoffeeServed response to sender")
    public void testResponseCoffeeServedOnServeCoffee() {
        TestProbe sender = TestProbe.apply(system);
        ActorRef waiter = system.actorOf(Waiter.props());
        waiter.tell(new Waiter.ServeCoffee(Coffee.AKKACCINO), sender.ref());
        sender.expectMsg(new Waiter.CoffeeServed(Coffee.AKKACCINO));
    }
}
