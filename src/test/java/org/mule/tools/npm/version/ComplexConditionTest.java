/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.npm.version;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ComplexConditionTest {
    @Test
    public void testSatisfies() throws Exception {
        assertTrue(new ComplexCondition(
                new SimpleCondition(">=", "0.0.1"),
                new SimpleCondition("<", "0.1.0")).satisfies("0.0.1"));
    }
}
