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
