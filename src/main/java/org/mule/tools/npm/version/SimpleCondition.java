/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.npm.version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleCondition implements Condition {
    private String condition;
    private final String version;
    private List<String> equalValues = Arrays.asList("=","<=", ">=");

    public SimpleCondition(String condition, String version) {
        this.condition = condition;
        this.version = version;
    }

    @Override
    public boolean satisfies(String otherVersion) {
        if (condition == null ||  "~".equals(condition) ) {
            return otherVersion.startsWith(version);
        } else if (equalValues.contains(condition)) {
            return this.version.equals(otherVersion);
        } else if (">".equals(condition)) {
            return greater(getVersion(otherVersion), getVersion(otherVersion));
        } else {
            return "<".equals(condition) &&
                    !greater(getVersion(otherVersion), getVersion(otherVersion));
        }
    }

    private boolean greater(List<Integer> left, List<Integer> right) {
        int minimum = left.size() >= right.size() ? right.size() : left.size();
        for ( int i = minimum - 1; i > 0; i++) {
            if ( !(left.get(i) > right.get(i)) ) {
                return false;
            }
        }
        return true;

    }

    private List<Integer> getVersion(String version) {
        String[] versionParts = version.split("\\.");
        List<Integer> versionComponents = new ArrayList<Integer>();
        for (String part : versionParts) {
            versionComponents.add(Integer.parseInt(part));
        }
        return versionComponents;
    }
}
