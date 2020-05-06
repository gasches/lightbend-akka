package com.lightbend.training.coffeehouse;

import java.util.Set;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CoffeeTest {

    @Test
    public void testBeverages() {
        assertEquals(Coffee.beverages(), Set.of(Coffee.AKKACCINO, Coffee.MOCHA_PLAY, Coffee.CAFFE_SCALA));
    }

    @DataProvider(name = "beverages")
    public Object[][] commands() {
        //@formatter:off
        return new Object[][] {
                {"A", Coffee.AKKACCINO},
                {"a", Coffee.AKKACCINO},
                {"M", Coffee.MOCHA_PLAY},
                {"m", Coffee.MOCHA_PLAY},
                {"C", Coffee.CAFFE_SCALA},
                {"c", Coffee.CAFFE_SCALA}
        };
        //@formatter:on
    }

    @Test(dataProvider = "beverages")
    public void testApply(String code, Coffee coffee) {
        assertEquals(Coffee.apply(code), coffee);
    }
}
