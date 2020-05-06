package com.lightbend.training.coffeehouse;

import java.util.Map;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CoffeeHouseAppTest {

    @Test
    public void testArgsToOpts() {
        Map<String, String> opts = CoffeeHouseApp.argsToOpts(new String[] {"a=1", "b", "-Dc=2"});
        assertEquals(opts, Map.of("a", "1", "-Dc", "2"));
    }

    @Test
    public void applySystemProperties() {
        System.setProperty("c", "");
        CoffeeHouseApp.applySystemProperties(Map.of("a", "1", "-Dc", "2"));
        assertEquals(System.getProperty("c"), "2");
    }
}