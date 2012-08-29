/**
 * Recess Maven Plugin
 * Copyright 2010-2011 (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mule.tools.npm;


import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

import java.io.File;

import static org.mockito.Mockito.mock;

public class NPMModuleTest {

    @Test
    public void testDownloadLess() throws Exception {
        NPMModule npmModule = NPMModule.fromNameAndVersion(mock(Log.class), "less", "1.0.32");
        npmModule.saveToFile(new File("target/less-test"));
    }

    @Test
    public void testDownloadRecess() throws Exception {
        NPMModule npmModule2 = NPMModule.fromName(mock(Log.class), "recess");
        npmModule2.saveToFileWithDependencies(new File("target/recess-test"));
    }

    @Test
    public void testDownloadJshint() throws Exception {
        NPMModule npmModule3 = NPMModule.fromQueryString(mock(Log.class), "jshint:0.8.1");
        npmModule3.saveToFileWithDependencies(new File("target/jshint-test"));
    }
}
