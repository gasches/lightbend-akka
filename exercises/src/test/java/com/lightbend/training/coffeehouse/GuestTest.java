package com.lightbend.training.coffeehouse;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import akka.actor.ActorRef;
import akka.testkit.EventFilter;
import akka.testkit.TestProbe;
import scala.concurrent.duration.FiniteDuration;

public class GuestTest extends BaseAkkaTest {

    @Test(description = "Sending CoffeeServed to Guest should result in increasing the coffeeCount and log a status message at info")
    public void testCoffeeServedIncreaseCoffeeCount() {
        //@formatter:off
        ActorRef guest = system.actorOf(Guest.props(system.deadLetters(), Coffee.AKKACCINO, FiniteDuration.apply(100, TimeUnit.MILLISECONDS)));
        EventFilter.info(null, guest.path().toString(), "", ".*[Ee]njoy.*1\\.*", 1)
                .intercept(Functions.wrap(() -> {
                    guest.tell(new Waiter.CoffeeServed(Coffee.AKKACCINO), guest);
                    return null;
                }), system);
        //@formatter:on
    }

    @Test(description = "Sending CoffeeServed to Guest should result in sending ServeCoffee to Waiter after finishCoffeeDuration")
    public void testCoffeeServedResultsInSendingServeCoffee() {
        TestProbe waiter = TestProbe.apply(system);
        ActorRef guest = createGuest(waiter);
        // The timer is not extremely accurate, relax the timing constraints.
        waiter.within(FiniteDuration.apply(50L, TimeUnit.MILLISECONDS), FiniteDuration.apply(200L, TimeUnit.MILLISECONDS),
                Functions.wrap(() -> {
                    guest.tell(new Waiter.CoffeeServed(Coffee.AKKACCINO), guest);
                    waiter.expectMsg(new Waiter.ServeCoffee(Coffee.AKKACCINO));
                    return null;
                }));
    }

    @Test(description = "Sending CoffeeFinished to Guest should result in sending ServeCoffee to Waiter")
    public void testCoffeeFinishedResultInServeCoffee() {
        TestProbe waiter = TestProbe.apply(system);
        ActorRef guest = createGuest(waiter);
        guest.tell(Guest.CoffeeFinished.INSTANCE, guest);
        waiter.expectMsg(new Waiter.ServeCoffee(Coffee.AKKACCINO));
    }

    private ActorRef createGuest(TestProbe waiter) {
        ActorRef guest = system.actorOf(
                Guest.props(waiter.ref(), Coffee.AKKACCINO, FiniteDuration.apply(100L, TimeUnit.MILLISECONDS)));
        // Creating Guest immediately sends Waiter.ServeCoffee
        waiter.expectMsg(new Waiter.ServeCoffee(Coffee.AKKACCINO));
        return guest;
    }
}
