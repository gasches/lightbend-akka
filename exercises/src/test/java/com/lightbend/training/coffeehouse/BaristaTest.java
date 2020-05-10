package com.lightbend.training.coffeehouse;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import akka.actor.ActorRef;
import akka.testkit.TestProbe;
import scala.concurrent.duration.FiniteDuration;

public class BaristaTest extends BaseAkkaTest {

    @Test(description = "Sending PrepareCoffee to Barista should result in sending a CoffeePrepared response after prepareCoffeeDuration")
    public void testPrepareCoffeeResultsInCoffeePrepared() {
        TestProbe sender = TestProbe.apply(system);
        ActorRef barista = system.actorOf(Barista.props(FiniteDuration.apply(100, TimeUnit.MILLISECONDS)));
        // busy is inaccurate, so we relax the timing constraints.
        sender.within(FiniteDuration.apply(50, TimeUnit.MILLISECONDS),
                FiniteDuration.apply(1000, TimeUnit.MILLISECONDS), Functions.wrap(() -> {
                    barista.tell(new Barista.PrepareCoffee(Coffee.AKKACCINO, system.deadLetters()), sender.ref());
                    sender.expectMsg(new Barista.CoffeePrepared(Coffee.AKKACCINO, system.deadLetters()));
                }));
    }
}
