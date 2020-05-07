package com.lightbend.training.coffeehouse;

import java.util.Map;

import org.testng.annotations.Test;

import akka.testkit.TestProbe;

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
        expectActor(new TestProbe(system), "/user/coffee-house");
    }
}