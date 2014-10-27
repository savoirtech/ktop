package com.savoirtech.karaf.commands;

import junit.framework.TestCase;

import com.savoirtech.karaf.commands.KTop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KTopTest extends TestCase {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(TestCase.class);

    public void testToMB() throws Exception {
        KTop test = new KTop();
        // One Megabyte
        assertEquals("1m", test.bToMB(1048576));
    }

}
