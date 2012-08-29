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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProgressListener implements ActionListener {

    private Log log;

    public ProgressListener(Log log) {
        this.log = log;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.log.debug("Downloaded bytes : " + ((DownloadCountingOutputStream) e.getSource()).getByteCount());
    }
}
