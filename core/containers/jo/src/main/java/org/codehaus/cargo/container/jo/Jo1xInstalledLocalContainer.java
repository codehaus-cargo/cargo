/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
package org.codehaus.cargo.container.jo;

import java.io.File;

import org.apache.tools.ant.types.FileSet;
import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.ServletContainerCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * jo! 1.1 container implementation.
 * 
 * @version $Id$
 * @see <a href="http://www.tagtraum.com">jo @ tagtraum</a>
 */
public class Jo1xInstalledLocalContainer extends AbstractInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jo1x";

    /**
     * Container name (humand-readable name).
     */
    private static final String NAME = "jo! 1.x";

    /**
     * Capabilities.
     */
    private ContainerCapability capability = new ServletContainerCapability();

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#AbstractInstalledLocalContainer(LocalConfiguration)
     */
    public Jo1xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * Implementation of {@link org.codehaus.cargo.container.LocalContainer#start()} to all
     * container extending this class must implement.
     * 
     * @param java the predefined JVM launcher to use to start the container
     * @throws Exception if any error is raised during the container start
     */
    @Override
    public void doStart(JvmLauncher java) throws Exception
    {
        // Invoke the main class
        FileSet fileSet = new FileSet();
        fileSet.setProject(getAntUtils().createProject());
        fileSet.setDir(new File(getHome()));
        fileSet.createInclude().setName("*.jar");
        fileSet.createInclude().setName("system/*.jar");
        for (String path : fileSet.getDirectoryScanner().getIncludedFiles())
        {
            java.addClasspathEntries(new File(fileSet.getDir(), path));
        }

        addToolsJarToClasspath(java);
        java.setMainClass("com.tagtraum.bootstrap.Bootstrap");
        java.addAppArguments("cl.jo=" + new File(getHome(), "lib") + "/*.jar");
        java.addAppArguments("main.jo=com.tagtraum.jo.JoIgnitionNG");
        java.addAppArguments(new File(getConfiguration().getHome(), "etc").toString() + "/");

        // set JO_HOME to configuration.dir
        java.setSystemProperty("JO_HOME", getConfiguration().getHome());

        java.start();
    }

    /**
     * Implementation of {@link org.codehaus.cargo.container.LocalContainer#stop()} to all container
     * extending this class must implement.
     * 
     * @param java the predefined JVM launcher to use to stop the container
     * @throws Exception if any error is raised during the container stop
     */
    @Override
    public void doStop(JvmLauncher java) throws Exception
    {
        FileSet fileSet = new FileSet();
        fileSet.setProject(getAntUtils().createProject());
        fileSet.setDir(new File(getHome()));
        fileSet.createInclude().setName("lib/*.jar");
        for (String path : fileSet.getDirectoryScanner().getIncludedFiles())
        {
            java.addClasspathEntries(new File(fileSet.getDir(), path));
        }

        java.setMainClass("com.tagtraum.metaserver.StopServer");
        java.addAppArguments(getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME));
        java.addAppArguments(getConfiguration().getPropertyValue(GeneralPropertySet.RMI_PORT));
        java.addAppArguments("MetaServer");

        java.execute();
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public String getName()
    {
        return NAME;
    }

    /**
     * @return the {@link org.codehaus.cargo.container.ContainerCapability} of the container in term
     * of ability to deploy such and such type of
     * {@link org.codehaus.cargo.container.deployable.Deployable}s (eg WAR, EAR, etc).
     */
    public ContainerCapability getCapability()
    {
        return this.capability;
    }

}
