package com.lightbend.training.coffeehouse;

import java.util.Map;

import org.testng.annotations.Test;

import akka.testkit.TestProbe;
import scala.collection.immutable.Seq;

import static org.testng.Assert.assertEquals;

public class CoffeeHouseAppTest extends BaseAkkaTest {

    @Test(description = "Calling argsToOpts should return the correct opts for the given args")
    public void testArgsToOpts() {
        Map<String, String> opts = CoffeeHouseApp.argsToOpts(new String[] {"a=1", "b", "-Dc=2"});
        assertEquals(opts, Map.of("a", "1", "-Dc", "2"));
    }

    @Test(description = "Calling applySystemProperties should apply the system properties for the given opts")
    public void testApplySystemProperties() {
        System.setProperty("c", "");
        CoffeeHouseApp.applySystemProperties(Map.of("a", "1", "-Dc", "2"));
        assertEquals(System.getProperty("c"), "2");
    }

    @Test(description = "Creating CoffeeHouseApp result in creating a top-level actor named 'coffee-house'")
    public void testActorCreation() {
        new CoffeeHouseApp(system, CoffeeHouseApp::createCoffeeHouse);
        expectActor(TestProbe.apply(system), "/user/coffee-house");
    }

    @Test(description = "Calling createGuest should result in sending CreateGuest to CoffeeHouse count number of times")
    public void testCreateGuest() {
        TestProbe probe = TestProbe.apply(system);
        CoffeeHouseApp coffeeHouseApp = new CoffeeHouseApp(system, s -> probe.ref());
        coffeeHouseApp.createGuest(2, Coffee.AKKACCINO, Integer.MAX_VALUE);
        assertEquals(probe.receiveN(2),
                Seq.fill(2, Functions.wrap(() -> new CoffeeHouse.CreateGuest(Coffee.AKKACCINO, Integer.MAX_VALUE))));
    }
}