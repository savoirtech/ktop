
package com.savoirtech.karaf.commands;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;

import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Date;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Formatter;
import java.io.IOException;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.AbstractAction;

@Command(scope = "aetos", name = "ktop", description = "Karaf Top Command")
public class ktop extends AbstractAction {

    private static final int DEFAULT_SLEEP_INTERVAL = 200;
 
    private boolean         sortByTotalCPU_         = false;
    private int             numberOfDisplayedThreads_ = 30;
    private boolean         displayedThreadLimit_     = true;
    private Map<Long, Long> previousThreadCPUMillis = new HashMap<Long, Long>();
 

    protected Object doExecute() throws Exception {
        try {
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
            ThreadMXBean threads = ManagementFactory.getThreadMXBean();
            MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
            ClassLoadingMXBean cl = ManagementFactory.getClassLoadingMXBean();
            //GarbageCollectorMXBean gc = ManagementFactory.getGarbageCollectorMXBean();
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

            System.out.printf(" ktop - %8tT, %6s, %2d cpus, %15.15s",  
                               new Date(), os.getArch(), os.getAvailableProcessors(), os.getName() + " "  + os.getVersion());
            System.out.printf(", load avg %3.2f%n", os.getSystemLoadAverage());
            System.out.printf(" UpTime: %-7s #Threads: %-4d #ThreadsPeak: %-4d #ThreadsCreated: %-4d %n",
                              toHHMM(runtime.getUptime()), threads.getThreadCount(),
                              threads.getPeakThreadCount(),
                              threads.getTotalStartedThreadCount());
            System.out.printf(" GC-Time: %-7s  #GC-Runs: %-8d%n",
                                0.0, 0);
                                //toHHMM(sumGCTimes(gc)), sumGCCount(gc),
                                //cl.getTotalLoadedClassCount());
            System.out.printf(" #CurrentClassesLoaded: %-8d #TotalClassesLoaded: %-8d #TotalClassesUnloaded: %-8d %n",
                               cl.getLoadedClassCount(), cl.getTotalLoadedClassCount(), cl.getUnloadedClassCount());
            System.out.printf(" CPU: %5.2f%% GC: %5.2f%% HEAP:%5s /%5s NONHEAP:%5s /%5s%n",
                               1.0, 1.0,
                               toMB(mem.getHeapMemoryUsage().getUsed()), toMB(mem.getHeapMemoryUsage().getMax()) ,
                               toMB(mem.getNonHeapMemoryUsage().getUsed()), toMB(mem.getNonHeapMemoryUsage().getMax()));
            System.out.println("==========================================================================================");
            System.out.printf("  TID   NAME                                              STATE    CPU  TOTALCPU BLOCKEDBY%n");
            printTopThreads(threads);
            System.out.println("==========================================================================================");
        }
    }

    private void printTopThreads(ThreadMXBean threads) {
        // Print top ten threads
        if (threads.isThreadCpuTimeSupported()) {
            // This JVM supports telling us thread stats!
            Map<Long, Long> newThreadCPUMillis = new HashMap<Long, Long>();

            Map<Long, Long> cpuTimeMap = new TreeMap<Long, Long>();

            for (Long tid : threads.getAllThreadIds()) {
                long threadCpuTime = threads.getThreadCpuTime(tid);
                long deltaThreadCpuTime = 0;
                if (previousThreadCPUMillis.containsKey(tid))
                {
                    deltaThreadCpuTime = threadCpuTime - previousThreadCPUMillis.get(tid);

                    cpuTimeMap.put(tid, deltaThreadCpuTime);
                }
                newThreadCPUMillis.put(tid, threadCpuTime);
            }

            cpuTimeMap = sortByValue(cpuTimeMap, true);

            int displayedThreads = 0;
            for (Long tid : cpuTimeMap.keySet()) {
                ThreadInfo info = threads.getThreadInfo(tid);
                displayedThreads++;
                if (displayedThreads > numberOfDisplayedThreads_
                   && displayedThreadLimit_) {
                   break;
                }
                if (info != null) {
                    System.out.printf(" %6d %-40s  %13s %5.2f%%    %5.2f%% %5s %n",
                                      tid,
                                      leftStr(info.getThreadName(), 40),
                                      info.getThreadState(),
                                      getThreadCPUUtilization(cpuTimeMap.get(tid), 200), 
                                      10.0,
                                      getBlockedThread(info));
                                      //getThreadCPUUtilization(cpuTimeMap.get(tid), vmInfo_.getDeltaUptime()),
                                      //getThreadCPUUtilization(threads.getThreadCpuTime(tid), 
                                      //                        vmInfo_.getProxyClient().getProcessCpuTime(), 1), 
                                      //                        getBlockedThread(info));
                }
            }
            if (newThreadCPUMillis.size() >= numberOfDisplayedThreads_
                && displayedThreadLimit_)
            {

            System.out.printf(" Note: Only top %d threads (according cpu load) are shown!",
                              numberOfDisplayedThreads_);
            System.out.println("");
            }
            previousThreadCPUMillis = newThreadCPUMillis;
        } else {
            System.out.printf("%n -Thread CPU telemetries are not available on the monitored jvm/platform-%n");
        }
    }


    public Map sortByValue(Map map, boolean reverse) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        if (reverse) {
            Collections.reverse(list);
        }

        Map result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public String leftStr(String str, int length) {
        return str.substring(0, Math.min(str.length(), length));
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

    private double getThreadCPUUtilization(long deltaThreadCpuTime,long totalTime, double factor) {
        if (totalTime == 0) {
            return 0;
        }
        return deltaThreadCpuTime / factor / totalTime * 100d;
    }

    public String toMB(long bytes) {
        if(bytes<0) {
            return "n/a";
        }
        return "" + (bytes / 1024 / 1024) + "m";
    }

    public String toHHMM(long millis) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        formatter.format("%2d:%2dm", millis / 1000 / 3600,(millis / 1000 / 60) % 60);
        return sb.toString();
    }

}
