package com.lightbend.training.coffeehouse;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class Waiter extends AbstractLoggingActor {

    public static Props props(ActorRef coffeeHouse, ActorRef barista, int maxComplaintCount) {
        return Props.create(Waiter.class, () -> new Waiter(coffeeHouse, barista, maxComplaintCount));
    }

    private final ActorRef coffeeHouse;
    private final ActorRef barista;
    private final int maxComplaintCount;

    private int complaintCount;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ServeCoffee.class, msg ->
                        coffeeHouse.tell(new CoffeeHouse.ApproveCoffee(msg.getCoffee(), sender()), self()))
                .match(Barista.CoffeePrepared.class, msg ->
                        msg.getGuest().tell(new CoffeeServed(msg.getCoffee()), self())
                ).match(Complaint.class, msg -> complaintCount >= maxComplaintCount, msg -> {
                    throw new FrustratedException(msg.getCoffee(), sender());
                }).match(Complaint.class, msg -> {
                    complaintCount += 1;
                    barista.tell(new Barista.PrepareCoffee(msg.getCoffee(), sender()), self());
                }).build();
    }

    @Value
    public static class ServeCoffee {
        Coffee coffee;
    }

    @Value
    public static class CoffeeServed {
        Coffee coffee;
    }

    @Value
    public static class Complaint {
        Coffee coffee;
    }

    @Getter
    public static class FrustratedException extends IllegalStateException {
        private final Coffee coffee;
        private final ActorRef guest;

        public FrustratedException(Coffee coffee, ActorRef guest) {
            super("Too many complaints!");
            this.coffee = coffee;
            this.guest = guest;
        }
    }
}
