
package com.savoirtech.karaf.commands;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;

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
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
            ThreadMXBean threads = ManagementFactory.getThreadMXBean();
            MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
            ClassLoadingMXBean cl = ManagementFactory.getClassLoadingMXBean();
            ktop(runtime, os, threads, mem, cl);
        } catch (IOException e) {
            //Ignore
        }
        return null;
    }

    private void ktop(RuntimeMXBean runtime, OperatingSystemMXBean os, ThreadMXBean threads, 
                      MemoryMXBean mem, ClassLoadingMXBean cl) throws InterruptedException, IOException {

        // Continously update stats to console.
        while (true) {
            Thread.sleep(DEFAULT_SLEEP_INTERVAL);
            //Clear console, then print JVM stats
            System.out.print("\33[2J");
            System.out.flush();
            System.out.print("\33[1;1H");
            System.out.flush();
            System.out.println("ktop:");
            System.out.println("Uptime " + runtime.getUptime());

            System.out.println("Threads");
            System.out.println("Live threads " + threads.getThreadCount());
            System.out.println("Daemon threads " + threads.getDaemonThreadCount());
            System.out.println("Peak " + threads.getPeakThreadCount());
            System.out.println("Total started " + threads.getTotalStartedThreadCount());

            System.out.println("Memory");
            System.out.println("Current heap size " + mem.getHeapMemoryUsage().getUsed());
            System.out.println("Maximum heap size " + mem.getHeapMemoryUsage().getMax());
            System.out.println("Committed heap size " +  mem.getHeapMemoryUsage().getCommitted());

            System.out.println("Classes");
            System.out.println("Current classes loaded " + cl.getLoadedClassCount());
            System.out.println("Total classes loaded " + cl.getTotalLoadedClassCount());
            System.out.println("Total classes unloaded " + cl.getUnloadedClassCount());
        }
    }
}
