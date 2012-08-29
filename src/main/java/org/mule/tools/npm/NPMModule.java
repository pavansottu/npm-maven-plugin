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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.logging.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NPMModule {

    private static String NPM_URL = "http://registry.npmjs.org/%s/%s";
    private String name;
    public String version;
    private Log log;
    private List<NPMModule> dependencies;
    private URL downloadURL;

    public String getName() {
        return name;
    }

    public String getVerion() {
        return version;
    }

    public List<NPMModule> getDependencies() {
        return dependencies;
    }

    public void saveToFileWithDependencies(File file) throws MojoExecutionException {
        this.saveToFile(file);

        for (NPMModule dependency : dependencies) {
            dependency.saveToFileWithDependencies(file);
        }
    }

    public void saveToFile(File file) throws MojoExecutionException {
        URL dl;
        OutputStream os = null;
        InputStream is = null;
        File outputFolderFileTmp = new File(file, name + "_tmp");
        File outputFolderFile = new File(file, name);

        if ( outputFolderFile.exists() ) {
            //Already downloaded nothing to do
            return;
        }


        outputFolderFileTmp.mkdirs();

        File tarFile = new File(outputFolderFileTmp, name + "-" + version + ".tgz");
        ProgressListener progressListener = new ProgressListener(log);
        try {
            os = new FileOutputStream(tarFile);
            is = getDownloadURL().openStream();

            DownloadCountingOutputStream dcount = new DownloadCountingOutputStream(os);
            dcount.setListener(progressListener);

            getDownloadURL().openConnection().getHeaderField("Content-Length");

            IOUtils.copy(is, dcount);

        } catch (FileNotFoundException e) {
            throw new MojoExecutionException(String.format("Error downloading module %s:%s", name,version),e);
        } catch (IOException e) {
            throw new MojoExecutionException(String.format("Error downloading module %s:%s", name,version),e);
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }

        final TarGZipUnArchiver ua = new TarGZipUnArchiver();
        ua.enableLogging(new LoggerAdapter(log));
        ua.setSourceFile(tarFile);
        ua.setDestDirectory(outputFolderFileTmp);
        ua.extract();

        FileUtils.deleteQuietly(tarFile);


        File fileToMove;

        File[] files = outputFolderFileTmp.listFiles();
        if (files != null && files.length == 1) {
            fileToMove = files[0];

        } else {
            File aPackage = new File(outputFolderFileTmp, "package");
            if (aPackage.exists() && aPackage.isDirectory()) {
                fileToMove = aPackage;
            } else {
                throw new MojoExecutionException(String.format("Only one file should be present at the folder when " +
                        "unpacking module %s:%s: ", name, version));
            }
        }

        try {
            FileUtils.moveDirectory(fileToMove, outputFolderFile);
        } catch (IOException e) {
            throw new MojoExecutionException(String.format("Error moving to the final folder when " +
                    "unpacking module %s:%s: ", name, version),e);
        }

        try {
            FileUtils.deleteDirectory(outputFolderFileTmp);
        } catch (IOException e) {
            log.info("Error while deleting temporary folder: " + outputFolderFileTmp, e);
        }

    }

    private void downloadDependencies(Map dependenciesMap) throws IOException, MojoExecutionException {
        for (Object dependencyAsObject :dependenciesMap.entrySet()){
            Map.Entry dependency = (Map.Entry) dependencyAsObject;
            String dependencyName = (String) dependency.getKey();

            String version = ((String) dependency.getValue());

            int i = version.lastIndexOf(" ");
            if ( i != -1) {
                version  = version.substring(i+1);
            }

            i = version.lastIndexOf("~");
            if ( i != -1) {
                version  = version.substring(i+1);
            }

            i = version.lastIndexOf("x");
            if ( i != -1) {
                version  = version.substring(0,i);
            }

            try {
                dependencies.add(fromNameAndVersion(log, dependencyName, version));
            } catch (MojoExecutionException e) {
                for (Object o : downloadMetadataList(dependencyName)) {
                    String dependencyVersion = (String) o;
                    if ( dependencyVersion.startsWith(version) ) {
                        dependencies.add(fromNameAndVersion(log, dependencyName, dependencyVersion));
                        return;
                    }
                }
                throw new RuntimeException("Error resolving dependency: " + dependencyName + ":" + version + " not found.");
            }
        }
    }

    private Set downloadMetadataList(String name) throws IOException, JsonParseException {
        URL dl = new URL(String.format(NPM_URL,name,""));
        ObjectMapper objectMapper = new ObjectMapper();
        Map allVersionsMetadata = objectMapper.readValue(dl,Map.class);
        return ((Map) allVersionsMetadata.get("versions")).keySet();
    }

    private Map downloadMetadata(String name, String version) throws IOException, JsonParseException {
        URL dl = new URL(String.format(NPM_URL,name,version != null ? version : "latest"));
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(dl, Map.class);
    }

    private void downloadModule() throws MojoExecutionException {

        try {
            Map jsonMap = downloadMetadata(name,version);

            Map distMap = (Map) jsonMap.get("dist");
            this.downloadURL = new URL((String) distMap.get("tarball"));
            this.version = (String) jsonMap.get("version");

            Map dependenciesMap = (Map) jsonMap.get("dependencies");

            if (dependenciesMap != null) {
                downloadDependencies(dependenciesMap);
            }

        } catch (MalformedURLException e) {
            throw new MojoExecutionException(String.format("Error downloading module info %s:%s", name,version),e);
        } catch (JsonMappingException e) {
            throw new MojoExecutionException(String.format("Error downloading module info %s:%s", name,version),e);
        } catch (JsonParseException e) {
            throw new MojoExecutionException(String.format("Error downloading module info %s:%s", name,version),e);
        } catch (IOException e) {
            throw new MojoExecutionException(String.format("Error downloading module info %s:%s", name,version),e);
        }
    }

    private NPMModule() {}

    public static NPMModule fromQueryString(Log log, String nameAndVersion) throws MojoExecutionException {
        String[] splitNameAndVersion = nameAndVersion.split(":");
        return fromNameAndVersion(log, splitNameAndVersion[0], splitNameAndVersion[1]);
    }

    public static NPMModule fromNameAndVersion(Log log, String name, String version) throws MojoExecutionException {
        NPMModule module = new NPMModule();
        module.log = log;
        module.name = name;
        module.version = version;
        module.dependencies = new ArrayList<NPMModule>();
        module.downloadModule();
        return module;
    }

    public URL getDownloadURL() {
        return downloadURL;
    }

    public static NPMModule fromName(Log log, String name) throws MojoExecutionException {
        return fromNameAndVersion(log, name, null);
    }

    private static class LoggerAdapter implements Logger {
        private Log log;
        private int threshold;

        public LoggerAdapter(Log log) {
            this.log = log;
        }
        @Override
        public void debug(String message) {
            log.debug(message);
        }

        @Override
        public void debug(String message, Throwable throwable) {
            log.debug(message,throwable);
        }

        @Override
        public boolean isDebugEnabled() {
            return log.isDebugEnabled();
        }

        @Override
        public void info(String message) {
            log.info(message);
        }

        @Override
        public void info(String message, Throwable throwable) {
            log.info(message,throwable);
        }

        @Override
        public boolean isInfoEnabled() {
            return log.isInfoEnabled();
        }

        @Override
        public void warn(String message) {
            log.warn(message);
        }

        @Override
        public void warn(String message, Throwable throwable) {
            log.warn(message,throwable);
        }

        @Override
        public boolean isWarnEnabled() {
            return log.isWarnEnabled();
        }

        @Override
        public void error(String message) {
            log.error(message);
        }

        @Override
        public void error(String message, Throwable throwable) {
            log.error(message,throwable);
        }

        @Override
        public boolean isErrorEnabled() {
            return log.isErrorEnabled();
        }

        @Override
        public void fatalError(String message) {
            log.error(message);
        }

        @Override
        public void fatalError(String message, Throwable throwable) {
            log.error(message,throwable);
        }

        @Override
        public boolean isFatalErrorEnabled() {
            return log.isErrorEnabled();
        }

        @Override
        public int getThreshold() {
            return threshold;
        }

        @Override
        public void setThreshold(int threshold) {
            this.threshold = threshold;
        }

        @Override
        public Logger getChildLogger(String name) {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }
    }
}
