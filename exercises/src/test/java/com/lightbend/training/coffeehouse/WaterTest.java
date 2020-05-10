package com.lightbend.training.coffeehouse;

import org.testng.annotations.Test;

import akka.actor.ActorRef;
import akka.testkit.ErrorFilter;
import akka.testkit.TestProbe;

public class WaterTest extends BaseAkkaTest {

    @Test(description = "Sending ServeCoffee to Waiter should result in sending ApproveCoffee to CoffeeHouse")
    public void testResponseCoffeeServedOnServeCoffee() {
        TestProbe coffeeHouse = TestProbe.apply(system);
        TestProbe guest = TestProbe.apply(system);
        ActorRef waiter = system.actorOf(Waiter.props(coffeeHouse.ref(), system.deadLetters(), Integer.MAX_VALUE));
        waiter.tell(new Waiter.ServeCoffee(Coffee.AKKACCINO), guest.ref());
        coffeeHouse.expectMsg(new CoffeeHouse.ApproveCoffee(Coffee.AKKACCINO, guest.ref()));
    }

    @Test(description = "Sending Complaint to Waiter should result in sending PrepareCoffee to Barista")
    public void testSendingComplaintResultsInPrepareCoffee() {
        TestProbe barista = TestProbe.apply(system);
        TestProbe guest = TestProbe.apply(system);
        ActorRef waiter = system.actorOf(Waiter.props(system.deadLetters(), barista.ref(), 1));
        waiter.tell(new Waiter.Complaint(Coffee.AKKACCINO), guest.ref());
        barista.expectMsg(new Barista.PrepareCoffee(Coffee.AKKACCINO, guest.ref()));
    }

    @Test(description = "Sending Complaint to Waiter should result in a FrustratedException if maxComplaintCount exceeded")
    public void testSendingComplaintResultsInFrustratedException() {
        ActorRef waiter = system.actorOf(Waiter.props(system.deadLetters(), system.deadLetters(), 0));
        new ErrorFilter(Waiter.FrustratedException.class, null, null, false, false, 1)
                .intercept(Functions.wrap(() -> waiter.tell(new Waiter.Complaint(Coffee.AKKACCINO), waiter)), system);
    }
}
