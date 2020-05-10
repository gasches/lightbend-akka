package com.lightbend.training.coffeehouse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.actor.Terminated;
import akka.japi.JavaPartialFunction;
import lombok.Value;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public class CoffeeHouse extends AbstractLoggingActor {

    public static Props props(int caffeineLimit) {
        return Props.create(CoffeeHouse.class, () -> new CoffeeHouse(caffeineLimit));
    }

    private final ActorRef barista;
    private final ActorRef waiter;
    private final FiniteDuration baristaPrepareCoffeeDuration;
    private final FiniteDuration guestFinishCoffeeDuration;
    private final int caffeineLimit;

    private final Map<ActorRef, Integer> guestBook = new HashMap<>();

    public CoffeeHouse(int caffeineLimit) {
        this.caffeineLimit = caffeineLimit;
        this.baristaPrepareCoffeeDuration = Duration.create(context().system().settings().config()
                        .getDuration("coffee-house.barista.prepare-coffee-duration", TimeUnit.MILLISECONDS),
                TimeUnit.MILLISECONDS);
        this.guestFinishCoffeeDuration = Duration.create(context().system().settings().config()
                        .getDuration("coffee-house.guest.finish-coffee-duration", TimeUnit.MILLISECONDS),
                TimeUnit.MILLISECONDS);
        this.barista = createBarista();
        this.waiter = createWaiter();
        log().debug("CoffeeHouse Open");
    }

    @Override
    public final SupervisorStrategy supervisorStrategy() {
        PartialFunction<Throwable, SupervisorStrategy.Directive> decider = new JavaPartialFunction<>() {
            @Override
            public SupervisorStrategy.Directive apply(Throwable ex, boolean isCheck) {
                if (ex instanceof Guest.CaffeineException) {
                    if (isCheck) {
                        return null;
                    }
                    return (SupervisorStrategy.Directive) SupervisorStrategy.stop();
                }
                throw noMatch();
            }
        };
        SupervisorStrategy baseSupervisorStrategy = super.supervisorStrategy();
        return OneForOneStrategy.apply(-1, Duration.Inf(), baseSupervisorStrategy.loggingEnabled(),
                decider.orElse(new JavaPartialFunction<>() {
                    @Override
                    public SupervisorStrategy.Directive apply(Throwable ex, boolean isCheck) {
                        return baseSupervisorStrategy.decider().apply(ex);
                    }
                }));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateGuest.class, msg -> {
                    ActorRef guest = createGuest(msg.getFavoriteCoffee(), msg.getCaffeineLimit());
                    guestBook.put(guest, 0);
                    log().info("Guest {} added to guest book.", guest);
                    context().watch(guest);
                }).match(ApproveCoffee.class,
                        msg -> guestBook.getOrDefault(msg.getGuest(), 0) < caffeineLimit,
                        msg -> {
                            guestBook.compute(msg.getGuest(), (k, v) -> (v == null ? 0 : v) + 1);
                            log().info("Guest {} caffeine count incremented.", msg.getGuest());
                            barista.forward(new Barista.PrepareCoffee(msg.getCoffee(), msg.getGuest()), context());
                }).match(ApproveCoffee.class, msg -> {
                    log().info("Sorry, {}, but you have reached your limit.", msg.getGuest());
                    context().stop(msg.getGuest());
                }).match(Terminated.class, msg -> {
                    log().info("Thanks, {}, for being our guest!", msg.actor());
                    guestBook.remove(msg.actor());
                }).build();
    }

    protected ActorRef createBarista() {
        return context().actorOf(Barista.props(baristaPrepareCoffeeDuration), "barista");
    }

    protected ActorRef createWaiter() {
        return context().actorOf(Waiter.props(self()), "waiter");
    }

    protected ActorRef createGuest(Coffee favoriteCoffee, int caffeineLimit) {
        return context().actorOf(Guest.props(waiter, favoriteCoffee, guestFinishCoffeeDuration, caffeineLimit));
    }

    @Value
    public static class CreateGuest {
        Coffee favoriteCoffee;
        int caffeineLimit;
    }

    @Value
    public static class ApproveCoffee {
        Coffee coffee;
        ActorRef guest;
    }
}
