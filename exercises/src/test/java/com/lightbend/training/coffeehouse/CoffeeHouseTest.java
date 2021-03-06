package com.lightbend.training.coffeehouse;

import org.testng.annotations.Test;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.EventFilter;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public class CoffeeHouseTest extends BaseAkkaTest {

    @Test(description = "Creating CoffeeHouse result in logging a status message at debug")
    public void testCreatingCoffeeHouse() {
        EventFilter.debug(null, null, "", ".*[Oo]pen.*", 1)
                .intercept(Functions.wrap(() -> system.actorOf(CoffeeHouse.props(Integer.MAX_VALUE), "coffee-house")),
                        system);
    }

    @Test(description = "Creating CoffeeHouse result in creating a child actor with the name 'barista'")
    public void testBaristaCreated() {
        system.actorOf(CoffeeHouse.props(Integer.MAX_VALUE), "create-barista");
        expectActor(TestProbe.apply(system), "/user/create-barista/barista");
    }

    @Test(description = "Creating CoffeeHouse result in creating a child actor with the name 'waiter'")
    public void testWaiterCreated() {
        system.actorOf(CoffeeHouse.props(Integer.MAX_VALUE), "create-waiter");
        expectActor(TestProbe.apply(system), "/user/create-waiter/waiter");
    }

    @Test(description = "Sending CreateGuest to CoffeeHouse should result in creating a Guest")
    public void testCreateGuestResultsInCreatingGuest() {
        ActorRef coffeeHouse = system.actorOf(CoffeeHouse.props(Integer.MAX_VALUE), "create-guest");
        coffeeHouse.tell(new CoffeeHouse.CreateGuest(Coffee.AKKACCINO, Integer.MAX_VALUE), coffeeHouse);
        expectActor(TestProbe.apply(system), "/user/create-guest/$*");
    }

    @Test(description = "Sending CreateGuest to CoffeeHouse should result in logging status guest added to guest book")
    public void testCreateGuestResultsInLoggingStatus() {
        ActorRef coffeeHouse = system.actorOf(CoffeeHouse.props(Integer.MAX_VALUE), "add-to-guest-book");
        EventFilter.info(null, coffeeHouse.path().toString(), "", ".*added to guest book.*", 1)
                .intercept(Functions.wrap(() ->
                        coffeeHouse.tell(new CoffeeHouse.CreateGuest(Coffee.AKKACCINO, Integer.MAX_VALUE), coffeeHouse)), system);
    }

    @Test(description = "Sending ApproveCoffee to CoffeeHouse should result in forwarding PrepareCoffee to Barista if caffeineLimit not yet reached")
    public void testApproveCoffeeResultsInPrepareCoffeeForwardingToBarista() {
        TestProbe barista = TestProbe.apply(system);
        TestActorRef<CoffeeHouse> coffeeHouse =
                TestActorRef.create(system, Props.create(CoffeeHouse.class, () -> new CoffeeHouse(Integer.MAX_VALUE) {
                    @Override
                    protected ActorRef createBarista() {
                        return barista.ref();
                    }
                }), "prepare-coffee");
        coffeeHouse.tell(new CoffeeHouse.CreateGuest(Coffee.AKKACCINO, Integer.MAX_VALUE), coffeeHouse);
        ActorRef guest = expectActor(TestProbe.apply(system), "/user/prepare-coffee/$*");
        coffeeHouse.tell(new CoffeeHouse.ApproveCoffee(Coffee.AKKACCINO, guest), coffeeHouse);
        barista.expectMsg(new Barista.PrepareCoffee(Coffee.AKKACCINO, guest));
    }

    @Test(description = "Sending ApproveCoffee to CoffeeHouse should result in Barista sending a CoffeePrepared to Waiter if caffeineLimit not yet reached")
    public void testApproveCoffeeResultsInBaristaSendingCoffeePreparedToWaiter() {
        // Just there to get an ActorRef...
        ActorRef dummyGuest = TestProbe.apply(system).ref();
        TestProbe dummyWaiter = TestProbe.apply(system);
        TestActorRef<CoffeeHouse> coffeeHouse =
                TestActorRef.create(system, Props.create(CoffeeHouse.class, () -> new CoffeeHouse(Integer.MAX_VALUE) {
                    @Override
                    protected ActorRef createBarista() {
                        return context().actorOf(Barista.props(FiniteDuration.Zero(), 100), "barista");
                    }
                }));
        coffeeHouse.tell(new CoffeeHouse.ApproveCoffee(Coffee.AKKACCINO, dummyGuest), dummyWaiter.ref());
        dummyWaiter.expectMsg(new Barista.CoffeePrepared(Coffee.AKKACCINO, dummyGuest));
    }

    @Test(description = "Sending ApproveCoffee to CoffeeHouse should result in logging status guest caffeine count incremented")
    public void testApproveCoffeeResultsInLoggingCaffeineCountIncremented() {
        TestProbe dummyGuest = TestProbe.apply(system);
        ActorRef coffeeHouse = system.actorOf(Props.create(CoffeeHouse.class, () -> new CoffeeHouse(Integer.MAX_VALUE) {
            @Override
            protected ActorRef createGuest(Coffee favoriteCoffee, int caffeineLimit) {
                return dummyGuest.ref();
            }
        }), "caffeine-count-incremented-guest-book");
        EventFilter.info(null, coffeeHouse.path().toString(), "", ".*caffeine count incremented.*", 1)
                .intercept(Functions.wrap(() -> {
                    coffeeHouse.tell(new CoffeeHouse.CreateGuest(Coffee.AKKACCINO, Integer.MAX_VALUE), coffeeHouse);
                    coffeeHouse.tell(new CoffeeHouse.ApproveCoffee(Coffee.AKKACCINO, dummyGuest.ref()), coffeeHouse);
                }), system);
    }

    @Test(description = "Sending ApproveCoffee to CoffeeHouse should result in logging a status message at info if caffeineLimit reached")
    public void testApproveCoffeeResultsInLoggingCaffeineLimitReached() {
        ActorRef coffeeHouse = system.actorOf(CoffeeHouse.props(0), "caffeine-limit");
        EventFilter.info(null, coffeeHouse.path().toString(), "", ".*[Ss]orry.*", 1)
                .intercept(Functions.wrap(() -> {
                    ActorRef guest = TestProbe.apply(system).ref();
                    coffeeHouse.tell(new CoffeeHouse.ApproveCoffee(Coffee.AKKACCINO, guest), coffeeHouse);
                }), system);
    }

    @Test(description = "Sending ApproveCoffee to CoffeeHouse should result in stopping the Guest if caffeineLimit reached")
    public void testApproveCoffeeResultsInStoppingGuestIfCaffeineLimitReached() {
        TestProbe probe = TestProbe.apply(system);
        ActorRef guest = TestProbe.apply(system).ref();
        probe.watch(guest);
        ActorRef coffeeHouse = system.actorOf(CoffeeHouse.props(0), "guest-terminated");
        coffeeHouse.tell(new CoffeeHouse.ApproveCoffee(Coffee.AKKACCINO, guest), coffeeHouse);
        probe.expectTerminated(guest, Duration.Undefined());
    }

    @Test(description = "On termination of Guest, CoffeeHouse should result in logging a thanks message at info")
    public void testLogThanksToGuestOnTermination() {
        ActorRef coffeeHouse = system.actorOf(CoffeeHouse.props(1), "thanks-coffee-house");
        EventFilter.info(null, coffeeHouse.path().toString(), "", ".*for being our guest.*", 1)
                .intercept(Functions.wrap(() -> {
                    coffeeHouse.tell(new CoffeeHouse.CreateGuest(Coffee.AKKACCINO, Integer.MAX_VALUE), coffeeHouse);
                    ActorRef guest = expectActor(TestProbe.apply(system), "/user/thanks-coffee-house/$*");
                    coffeeHouse.tell(new CoffeeHouse.ApproveCoffee(Coffee.AKKACCINO, guest), coffeeHouse);
                }), system);
    }

    @Test(description = "On failure of Guest CoffeeHouse should stop it")
    public void testStopGuestOnFailure() {
        TestProbe probe = TestProbe.apply(system);
        ActorRef coffeeHouse = system.actorOf(CoffeeHouse.props(1), "guest-stopped");
        coffeeHouse.tell(new CoffeeHouse.CreateGuest(Coffee.AKKACCINO, 0), coffeeHouse);
        ActorRef guest = expectActor(TestProbe.apply(system), "/user/guest-stopped/$*");
        probe.watch(guest);
        coffeeHouse.tell(new CoffeeHouse.ApproveCoffee(Coffee.AKKACCINO, guest), coffeeHouse);
        probe.expectTerminated(guest, Duration.Undefined());
    }

    @Test(description = "On failure of Guest CoffeeHouse restart it and resend PrepareCoffee to Barista")
    public void testResendPrepareCoffeeOnFailure() {
        TestProbe barista = TestProbe.apply(system);
        TestActorRef.create(system, Props.create(CoffeeHouse.class, () -> new CoffeeHouse(Integer.MAX_VALUE) {
            @Override
            protected ActorRef createBarista() {
                return barista.ref();
            }

            @Override
            protected ActorRef createWaiter() {
                return context().actorOf(Props.create(FrustratedWaiter.class, FrustratedWaiter::new), "waiter");
            }
        }), "resend-prepare-coffee");
        ActorRef waiter = expectActor(TestProbe.apply(system), "/user/resend-prepare-coffee/waiter");
        waiter.tell("blow-up", waiter);
        barista.expectMsg(new Barista.PrepareCoffee(Coffee.AKKACCINO, system.deadLetters()));
    }

    private static class FrustratedWaiter extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder().matchAny(msg -> {
                throw new Waiter.FrustratedException(Coffee.AKKACCINO, context().system().deadLetters());
            }).build();
        }
    }
}