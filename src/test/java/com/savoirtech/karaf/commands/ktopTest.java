package com.savoirtech.karaf.commands;

import junit.framework.TestCase;

import com.savoirtech.karaf.commands.ktop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ktopTest extends TestCase {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(TestCase.class);

    public void testToMB() throws Exception {
        ktop test = new ktop();
        // One Megabyte
        assertEquals("1m", test.toMB(1048576));
    }

}
