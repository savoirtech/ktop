
package com.savoirtech.karaf.commands;

import java.io.IOException;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.AbstractAction;

@Command(scope = "aetos", name = "ktop", description = "Karaf Top Command")
public class ktop extends AbstractAction {

    private static final int DEFAULT_SLEEP_INTERVAL = 200;

    protected Object doExecute() throws Exception {
        try {
            ktop();
        } catch (IOException e) {
            //Ignore
        }
        return null;
    }

    private void ktop() throws InterruptedException, IOException {

        // Continously update stats to console.
        while (true) {
            Thread.sleep(DEFAULT_SLEEP_INTERVAL);
            //Clear console, then print JVM stats
            System.out.print("\33[2J");
            System.out.flush();
            System.out.print("\33[1;1H");
            System.out.flush();
            System.out.println("Hello World " + System.currentTimeMillis());
        }
    }
}
