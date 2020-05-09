package com.lightbend.training.coffeehouse;

import org.testng.annotations.Test;

import akka.actor.ActorRef;
import akka.testkit.TestProbe;

public class WaterTest extends BaseAkkaTest {

    @Test(description = "Sending ServeCoffee to Waiter should result in sending a CoffeeServed response to sender")
    public void testResponseCoffeeServedOnServeCoffee() {
        TestProbe barista = TestProbe.apply(system);
        TestProbe guest = TestProbe.apply(system);
        ActorRef waiter = system.actorOf(Waiter.props(barista.ref()));
        waiter.tell(new Waiter.ServeCoffee(Coffee.AKKACCINO), guest.ref());
        barista.expectMsg(new Barista.PrepareCoffee(Coffee.AKKACCINO, guest.ref()));
    }
}
