package com.lightbend.training.coffeehouse;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import akka.actor.ActorRef;
import akka.japi.JavaPartialFunction;
import akka.testkit.TestProbe;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import static org.testng.Assert.assertTrue;

public class BaristaTest extends BaseAkkaTest {

    @Test(description = "Sending PrepareCoffee to Barista should result in sending a CoffeePrepared response after prepareCoffeeDuration")
    public void testPrepareCoffeeResultsInCoffeePrepared() {
        TestProbe sender = TestProbe.apply(system);
        ActorRef barista = system.actorOf(Barista.props(FiniteDuration.apply(100, TimeUnit.MILLISECONDS), 100));
        // The timer is not extremely accurate, so we relax the timing constraints.
        sender.within(FiniteDuration.apply(50, TimeUnit.MILLISECONDS),
                FiniteDuration.apply(200, TimeUnit.MILLISECONDS), Functions.wrap(() -> {
                    barista.tell(new Barista.PrepareCoffee(Coffee.AKKACCINO, system.deadLetters()), sender.ref());
                    sender.expectMsg(new Barista.CoffeePrepared(Coffee.AKKACCINO, system.deadLetters()));
                }));
    }

    @Test(description = "Sending PrepareCoffee to Barista should result in sending a CoffeePrepared response with a random Coffee for an inaccurate one")
    public void testPrepareCoffeeResultsInCoffeePreparedWithRandomCoffee() {
        TestProbe waiter = TestProbe.apply(system);
        int accuracy = 50;
        int runs = 1000;
        ActorRef barista = system.actorOf(Barista.props(FiniteDuration.Zero(), accuracy));
        ActorRef guest = system.deadLetters();
        ArrayList<Coffee> coffees = new ArrayList<>();
        JavaPartialFunction<Object, Coffee> f = new JavaPartialFunction<>() {
            @Override
            public Coffee apply(Object o, boolean isCheck) {
                if (o instanceof Barista.CoffeePrepared) {
                    Barista.CoffeePrepared msg = (Barista.CoffeePrepared) o;
                    if (msg.getGuest().equals(guest)) {
                        if (isCheck) {
                            return null;
                        }
                        return msg.getCoffee();
                    }
                }
                throw noMatch();
            }
        };
        for (int i = 0; i < runs; i++) {
            barista.tell(new Barista.PrepareCoffee(Coffee.AKKACCINO, guest), waiter.ref());
            coffees.add(waiter.expectMsgPF(Duration.Undefined(), "", f));
        }
        int expectedCount = runs * accuracy / 100;
        int variation = expectedCount / 10;
        long count = coffees.stream().filter(c -> c == Coffee.AKKACCINO).count();
        assertTrue(count >= expectedCount - variation && count <= expectedCount + variation);
    }
}
