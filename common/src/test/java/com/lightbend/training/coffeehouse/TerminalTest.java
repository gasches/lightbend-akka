package com.lightbend.training.coffeehouse;

import com.lightbend.training.coffeehouse.Terminal.Command;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class TerminalTest {

    @DataProvider(name = "commands")
    public Object[][] commands() {
        //@formatter:off
        return new Object[][] {
                {"guest", new Terminal.Guest(1, Coffee.AKKACCINO, Integer.MAX_VALUE)},
                {"2 g", new Terminal.Guest(2, Coffee.AKKACCINO, Integer.MAX_VALUE)},
                {"g m", new Terminal.Guest(1, Coffee.MOCHA_PLAY, Integer.MAX_VALUE)},
                {"g 1", new Terminal.Guest(1, Coffee.AKKACCINO, 1)},
                {"2 g m 1", new Terminal.Guest(2, Coffee.MOCHA_PLAY, 1)},
                {"status", Terminal.Status.INSTANCE},
                {"s", Terminal.Status.INSTANCE},
                {"quit", Terminal.Quit.INSTANCE},
                {"q", Terminal.Quit.INSTANCE},
                {"foo", new Terminal.Unknown("foo")}
        };
        //@formatter:on
    }

    @Test(dataProvider = "commands")
    public void testParser(String commandStr, Command command) {
        assertEquals(Terminal.parseAsCommand(commandStr), command);
    }
}