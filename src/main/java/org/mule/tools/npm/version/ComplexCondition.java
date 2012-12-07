/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.npm.version;

public class ComplexCondition implements Condition {

    private final SimpleCondition left;
    private final SimpleCondition right;

    public ComplexCondition(SimpleCondition left, SimpleCondition right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean satisfies(String version) {
        return left.satisfies(version) && right.satisfies(version);
    }
}
