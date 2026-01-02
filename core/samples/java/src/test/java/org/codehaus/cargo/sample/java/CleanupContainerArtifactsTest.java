/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ========================================================================
 */
package org.codehaus.cargo.sample.java;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;

/**
 * Cleans up older versions of container artifacts in the <code>installs</code> directory.
 */
public class CleanupContainerArtifactsTest
{
    /**
     * Cleans up older versions of container artifacts in the <code>installs</code> directory.
     */
    @Test
    public void testCleanupContainerArtifacts()
    {
        File installsDir = new File(System.getProperty("cargo.download.dir"));
        if (installsDir.isDirectory())
        {
            FileHandler fileHandler = new DefaultFileHandler();
            // Read the pom.xml file as a String, so we also can match older versions of a certain
            // container - For example, the Tomcat 8.0.x and 8.5.x ZIP files, which are both for
            // the tomcat8x container. If we read the pom.xml file as an XML / Maven Model, we
            // would miss out the "alternative" versions (which are in various profiles).
            String pomXml = fileHandler.readTextFile(
                fileHandler.append(System.getProperty("basedir"), "../pom.xml"),
                    StandardCharsets.UTF_8);
            for (File installFile : installsDir.listFiles())
            {
                String installFilename = installFile.getName();
                if (installFile.isFile() && !pomXml.contains(installFilename)
                    && (installFilename.endsWith(".tar.gz") || installFilename.endsWith(".zip")))
                {
                    System.out.println("Deleting obsolete container artifact: " + installFile);
                    installFile.delete();
                }
            }
        }
    }
}
