ktop
====

Karaf Top Command

 ktop - display and update sorted information about JVM and threads.

Description:

 The ktop command displays key JVM metrics, and updates a sorted list
 of thread statistics. 

Building from source:
===

To build, invoke:
 
 mvn install


To install in Karaf, invoke from console:

 install -s mvn:com.savoirtech.karaf.commands/ktop


To execute command on Karaf, invoke:

 aetos:ktop


To exit ktop, press control + c


Runtime Options:
===

 -t  --threads   number of threads to display. 
 
 -u  --updates   information update interval in milliseconds.
