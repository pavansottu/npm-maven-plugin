/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.npm;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.File;

/**
 * Goal that offers Recess support in Maven builds.
 *
 * @goal fetch-modules
 * @phase generate-sources
 */
public class NPMMojo extends AbstractJavascriptMojo {

    /**
     * Where the resulting files will be downloaded.
     *
     * @parameter expression="${recess.outputDirectory}" default-value="${basedir}/src/main/resources/META-INF"
     */
    private File outputDirectory;

    /**
     * The identifiers of the packages to download. Use the following syntax: package:version
     *
     * @parameter expression="${recess.packages}
     * @required
     */
    private String [] packages;

    public void execute() throws MojoExecutionException {
        Log log = getLog();

        for (String aPackage : packages) {
            NPMModule.fromQueryString(log,aPackage).saveToFileWithDependencies(outputDirectory);
        }
    }
}
