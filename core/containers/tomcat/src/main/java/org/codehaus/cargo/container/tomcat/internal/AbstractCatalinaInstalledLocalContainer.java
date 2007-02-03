/* 
 * ========================================================================
 * 
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
 * 
 * Copyright 2004-2006 Vincent Massol.
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
package org.codehaus.cargo.container.tomcat.internal;

import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.codehaus.cargo.container.internal.AntContainerExecutorThread;
import org.codehaus.cargo.container.configuration.LocalConfiguration;

import java.io.File;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Base support for Catalina based containers.
 * 
 * @version $Id$
 */
public abstract class AbstractCatalinaInstalledLocalContainer
    extends AbstractTomcatInstalledLocalContainer
{
    /**
     * Parsed version of the container.
     */
    private String version;

    /**
     * {@inheritDoc}
     * @see LocalConfiguration#configure(org.codehaus.cargo.container.LocalContainer)
     */
    public AbstractCatalinaInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * Returns the version of the Tomcat installation.
     * 
     * @return The Tomcat version, or <code>null</code> if the version number could not be retrieved
     * @param defaultVersion default version to use if we cannot find out the exact Tomcat version
     */
    protected final String getVersion(String defaultVersion)
    {
        String version = this.version;
        
        if (version == null)
        {
            try
            {
                // Unfortunately, there's no safe way to find out the version of a Catalina 
                // installation, so we need to try multiple paths here

                // Tomcat 4.1.0 and later includes a ServerInfo.properties resource in catalina.jar
                // that contains the version number. If that resource doesn't exist, we're on 
                // Tomcat 4.0.x
                JarFile catalinaJar = new JarFile(
                    new File(getHome(), "server/lib/catalina.jar"));
                ZipEntry entry = catalinaJar.getEntry(
                    "org/apache/catalina/util/ServerInfo.properties");
                if (entry != null)
                {
                    Properties props = new Properties();
                    props.load(catalinaJar.getInputStream(entry));
                    String serverInfo = props.getProperty("server.info");
                    if (serverInfo.indexOf('/') > 0)
                    {
                        version = serverInfo.substring(serverInfo.indexOf('/') + 1);
                    }
                }
                else
                {
                    version = "4.0.x";
                }
            }
            catch (Exception e)
            {
                version = defaultVersion;
                getLogger().debug("Failed to find Tomcat version, base error [" + e.getMessage()
                    + "]", this.getClass().getName());
            }

            getLogger().debug("Parsed Tomcat version = [" + version + "]",
                this.getClass().getName());
            
            this.version = version;
        }
        
        return version;
    }

    /**
     * {@inheritDoc}
     * @see AbstractTomcatInstalledLocalContainer#invokeContainer(String, Java)
     */
    protected final void invokeContainer(String action, Java java) throws Exception
    {
        java.addSysproperty(getAntUtils().createSysProperty("catalina.home", getHome()));
        java.addSysproperty(getAntUtils().createSysProperty("catalina.base", 
            getConfiguration().getHome()));
        File tempFile = new File(getConfiguration().getHome(), "temp");
        java.addSysproperty(getAntUtils().createSysProperty("java.io.tmpdir", tempFile));
        Path classpath = java.createClasspath();
        classpath.createPathElement().setLocation(new File(getHome(), "bin/bootstrap.jar"));
        addToolsJarToClasspath(classpath);
        java.setClassname("org.apache.catalina.startup.Bootstrap");
        java.createArg().setValue(action);
        Thread catalinaRunner = new AntContainerExecutorThread(java);
        catalinaRunner.start();
    }
}
