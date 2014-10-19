
package com.savoirtech.karaf.commands;

import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.commands.Argument;

@Command(scope = "aetos", name = "ktop", description = "Karaf Top Command")
public class ktop extends OsgiCommandSupport {

    @Option(name = "-o", aliases = { "--option" }, description = "An option to the command", required = false, multiValued = false)
    private String option;

    @Argument(name = "argument", description = "Argument to the command", required = false, multiValued = false)
    private String argument;

    protected Object doExecute() throws Exception {
         System.out.println("Executing command ktop");
         System.out.println("Option: " + option);
         System.out.println("Argument: " + argument);
         return null;
    }
}
