/*
 * KTop
 *
 * Copyright (c) 2014, Savoir Technologies, Inc., All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */

package com.savoirtech.karaf.commands;

import java.io.InputStreamReader;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;

import java.lang.management.ThreadInfo;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Formatter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.AbstractAction;

import static java.util.concurrent.TimeUnit.*;

@Command(scope = "aetos", name = "ktop", description = "Karaf Top Command")
public class KTop extends AbstractAction {

    private int             DEFAULT_REFRESH_INTERVAL = 1000;
    private int             DEFAULT_KEYBOARD_INTERVAL = 100;
    private int             numberOfDisplayedThreads = 30; 
    private long            lastUpTime               = 0;
    private Map<Long, Long> previousThreadCPUTime = new HashMap<Long, Long>();
    private int sortIndex = 3;
    private boolean reverseSort = true;

    @Option(name = "-t", aliases = { "--threads" }, description = "Number of threads to display", required = false, multiValued = false)
    private String numThreads;

    @Option(name = "-u", aliases = { "--updates" }, description = "Update interval in milliseconds", required = false, multiValued = false)
    private String updates;


    protected Object doExecute() throws Exception {
        if (numThreads != null) {
             numberOfDisplayedThreads = Integer.parseInt(numThreads);
        }
        if (updates != null) {
             DEFAULT_REFRESH_INTERVAL = Integer.parseInt(updates);
        } 
        try {
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
            ThreadMXBean threads = ManagementFactory.getThreadMXBean();
            MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
            ClassLoadingMXBean cl = ManagementFactory.getClassLoadingMXBean();
            KTop(runtime, os, threads, mem, cl);
        } catch (IOException e) {
            //Ignore
        }
        return null;
    }

    private void KTop(RuntimeMXBean runtime, OperatingSystemMXBean os, ThreadMXBean threads, 
                      MemoryMXBean mem, ClassLoadingMXBean cl) throws InterruptedException, IOException {

        Thread.currentThread().setName("ktop");
        boolean run = true;

        // Continously update stats to console.
        while (run) {
            //Clear console, then print JVM stats
            clearScreen();
            printOperatingSystemHeader(os);
            printThreadsHeader(runtime, threads);
            printGCHeader(); 
            printClassLoaderHeader(cl);
            printJVMHeader(mem);
            System.out.println("\u001B[36m==========================================================================================\u001B[0m");
            System.out.printf("    TID THREAD NAME                                       STATE    CPU  CPU-TIME BLOCKEDBY%n");
            printTopThreads(threads, runtime);
            // Display notifications
            System.out.printf(" Note: Only top %d threads (according cpu load) are shown!", numberOfDisplayedThreads);
            System.out.println();
            System.out.printf(" Note: Thread stats updated at  %d ms intervals", DEFAULT_REFRESH_INTERVAL);
            System.out.println();
            System.out.println(" To exit ktop: q, Sorting: < or >, Reverse sort: r");
            System.out.println();
            System.out.println("\u001B[36m==========================================================================================\u001B[0m");

            run = waitOnKeyboard();
        }
    }

    private boolean waitOnKeyboard() throws InterruptedException {
        InputStreamReader reader = new InputStreamReader(session.getKeyboard());
        for (int i = 0; i < DEFAULT_REFRESH_INTERVAL / DEFAULT_KEYBOARD_INTERVAL; i++) {
            Thread.sleep(DEFAULT_KEYBOARD_INTERVAL);
            try {
                if (reader.ready()) {
                    int value = reader.read();
                    switch (value) {
                        case 'q':
                            return false;
                        case 'r':
                            reverseSort = !reverseSort;
                            break;
                        case '<':
                            if (sortIndex > 0) {
                                sortIndex--;
                            }
                            break;
                        case '>':
                            if (sortIndex < 5) {
                                sortIndex++;
                            }
                            break;
                    }
                }
            } catch (IOException e) {

            }
        }

        return true;
    }

    private void clearScreen() {
        System.out.print("\33[2J");
        System.out.flush();
        System.out.print("\33[1;1H");
        System.out.flush();
    }

    private void printOperatingSystemHeader(OperatingSystemMXBean os) {
        System.out.printf(" \u001B[1mktop\u001B[0m - %8tT, %6s, %2d cpus, %15.15s",
                          new Date(), os.getArch(), os.getAvailableProcessors(), 
                          os.getName() + " "  + os.getVersion());
        if (os.getSystemLoadAverage() != -1) {
            System.out.printf(", CPU load avg %3.2f%n", os.getSystemLoadAverage());
        } else {
            System.out.printf("%n");
        }
    }

    private void printThreadsHeader(RuntimeMXBean runtime, ThreadMXBean threads) {
        System.out.printf(" UpTime: %-7s #Threads: %-4d #ThreadsPeak: %-4d #ThreadsCreated: %-4d %n",
                          timeUnitToHoursMinutes(MILLISECONDS, runtime.getUptime()), threads.getThreadCount(),
                          threads.getPeakThreadCount(),
                          threads.getTotalStartedThreadCount());
    }

    private void printGCHeader() {
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            float time = gc.getCollectionTime();
            System.out.printf(" Garbage collector: Name: %s Collections: %-5d Time: %5.3f ms %n",
                              gc.getName(), gc.getCollectionCount(), time );
        }
    }

    private void printClassLoaderHeader(ClassLoadingMXBean cl) {
        System.out.printf(" #CurrentClassesLoaded: %-8d #TotalClassesLoaded: %-8d #TotalClassesUnloaded: %-8d %n",
                          cl.getLoadedClassCount(), cl.getTotalLoadedClassCount(), cl.getUnloadedClassCount());
    }

    private void printJVMHeader(MemoryMXBean mem) {
        System.out.printf(" JVM Memory: HEAP:%5s /%5s NONHEAP:%5s /%5s%n",
                          bToMB(mem.getHeapMemoryUsage().getUsed()), bToMB(mem.getHeapMemoryUsage().getMax()) ,
                          bToMB(mem.getNonHeapMemoryUsage().getUsed()), bToMB(mem.getNonHeapMemoryUsage().getMax()));
    }

    private void printTopThreads(ThreadMXBean threads, RuntimeMXBean runtime) {
        // Test if this JVM supports telling us thread stats!
        if (threads.isThreadCpuTimeSupported()) {

            long uptime = runtime.getUptime();
            long deltaUpTime = uptime - lastUpTime;
            lastUpTime = uptime;

            Map<Long, Object[]> stats = getThreadStats(threads, deltaUpTime);

            List<Long> sortedKeys = sortByValue(stats);

            // Display threads
            printThreads(sortedKeys, stats);

        } else {
            System.out.printf("%n -Thread CPU metrics are not available on this jvm/platform-%n");
        }
    }

    private void printThreads(List<Long> sortedKeys, Map<Long, Object[]> stats) {
        int displayedThreads = 0;
        for (Long tid : sortedKeys) {

            displayedThreads++;
            if (displayedThreads > numberOfDisplayedThreads) {
                break; // We're done displaying threads.
            }

            System.out.printf(" %6d %-40s  %13s %5.2f%% %8s %5s %n",
                              stats.get(tid)[0],
                              stats.get(tid)[1],
                              stats.get(tid)[2],
                              stats.get(tid)[3],
                              stats.get(tid)[4],
                              stats.get(tid)[5]);
        }
    }

    private Map<Long, Object[]> getThreadStats(ThreadMXBean threads, long deltaUpTime) {
        Map<Long, Object[]> allStats = new HashMap<Long, Object[]>();

        for (Long tid : threads.getAllThreadIds()) {

            ThreadInfo info = threads.getThreadInfo(tid);

            if (info != null) {
                Object[] stats = new Object[6];
                long threadCpuTime = threads.getThreadCpuTime(tid);
                long deltaThreadCpuTime;

                if (previousThreadCPUTime.containsKey(tid)) {
                    deltaThreadCpuTime = threadCpuTime - previousThreadCPUTime.get(tid);
                }
                else {
                    deltaThreadCpuTime = threadCpuTime;
                }

                previousThreadCPUTime.put(tid, threadCpuTime);

                String name = info.getThreadName();
                stats[0] = tid;
                stats[1] = name.substring(0, Math.min(name.length(), 40));
                stats[2] = info.getThreadState();
                stats[3] = getThreadCPUUtilization(deltaThreadCpuTime, deltaUpTime);
                stats[4] = timeUnitToMinutesSeconds(NANOSECONDS, threads.getThreadCpuTime(tid));
                stats[5] = getBlockedThread(info);

                allStats.put(tid, stats);
            }
        }

        return allStats;
    }

    public List sortByValue(Map map) {
        List<Map.Entry> list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry>() {

            public int compare(Map.Entry o1, Map.Entry o2) {
                Comparable c1 = ((Comparable) (((Object[]) o1.getValue())[sortIndex]));
                Comparable c2 = ((Comparable) (((Object[]) o2.getValue())[sortIndex]));
                return c1.compareTo(c2);
            }
        });

        if (reverseSort) {
            Collections.reverse(list);
        }

        List result = new ArrayList();
        for (Iterator<Map.Entry> it = list.iterator(); it.hasNext();) {
            result.add(it.next().getKey());
        }
        return result;
    }

    private String getBlockedThread(ThreadInfo info) {
        if (info.getLockOwnerId() >= 0) {
            return "" + info.getLockOwnerId();
        } else {
            return "";
        }
    }

    private double getThreadCPUUtilization(long deltaThreadCpuTime, long totalTime) {
        return getThreadCPUUtilization(deltaThreadCpuTime, totalTime, 1000 * 1000);
    }

    private double getThreadCPUUtilization(long deltaThreadCpuTime, long totalTime, double factor) {
        if (totalTime == 0) {
            return 0;
        }
        return deltaThreadCpuTime / factor / totalTime * 100d;
    }

    public String bToMB(long bytes) {
        if(bytes<0) {
            return "n/a";
        }
        return "" + (bytes / 1024 / 1024) + "m";
    }

    public String timeUnitToHoursMinutes(TimeUnit timeUnit, long value) {
        if (value == -1) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        long hours = HOURS.convert(value, timeUnit);
        long minutes = MINUTES.convert(value, timeUnit) - MINUTES.convert(hours, HOURS);
        formatter.format("%2d:%02dm", hours, minutes);
        return sb.toString();
    }

    public String timeUnitToMinutesSeconds(TimeUnit timeUnit, long value) {
        if (value == -1) {
            return "0";
        }
        long valueRemaining = value;
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);

        long minutes = MINUTES.convert(valueRemaining, timeUnit);
        valueRemaining = valueRemaining - timeUnit.convert(minutes, MINUTES);
        long seconds = SECONDS.convert(valueRemaining, timeUnit);
        valueRemaining = valueRemaining - timeUnit.convert(seconds, SECONDS);
        long nanoseconds = NANOSECONDS.convert(valueRemaining, timeUnit);
        // min so that 99.5+ does not show up as 100 hundredths of a second
        int hundredthsOfSecond = Math.min(Math.round(nanoseconds / 10000000f), 99);
        formatter.format("%2d:%02d.%02d", minutes, seconds, hundredthsOfSecond);
        return sb.toString();
    }

}
