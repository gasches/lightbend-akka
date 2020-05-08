package com.lightbend.training.coffeehouse;

import org.testng.annotations.Test;

import akka.actor.ActorRef;
import akka.testkit.EventFilter;
import akka.testkit.TestProbe;

public class GuestTest extends BaseAkkaTest {

    @Test(description = "Sending CoffeeServed to Guest should result in increasing the coffeeCount and log a status message at info")
    public void testCoffeeServedIncreaseCoffeeCount() {
        //@formatter:off
        ActorRef guest = system.actorOf(Guest.props(system.deadLetters(), Coffee.AKKACCINO));
        EventFilter.info(null, guest.path().toString(), "", ".*[Ee]njoy.*1\\.*", 1)
                .intercept(Functions.wrap(() -> {
                    guest.tell(new Waiter.CoffeeServed(Coffee.AKKACCINO), guest);
                    return null;
                }), system);
        //@formatter:on
    }

    @Test(description = "Sending CoffeeFinished to Guest should result in sending ServeCoffee to Waiter")
    public void testCoffeeFinishedResultInServeCoffee() {
        TestProbe waiter = TestProbe.apply(system);
        ActorRef guest = system.actorOf(Guest.props(waiter.ref(), Coffee.AKKACCINO));
        guest.tell(Guest.CoffeeFinished.INSTANCE, guest);
        waiter.expectMsg(new Waiter.ServeCoffee(Coffee.AKKACCINO));
    }
}
