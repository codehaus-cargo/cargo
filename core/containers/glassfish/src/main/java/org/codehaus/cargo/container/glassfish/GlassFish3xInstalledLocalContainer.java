/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.container.glassfish;

import java.io.File;

import org.apache.tools.ant.taskdefs.Java;
import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.glassfish.internal.AbstractGlassFishInstalledLocalContainer;
import org.codehaus.cargo.container.internal.AntContainerExecutorThread;
import org.codehaus.cargo.container.spi.deployer.AbstractLocalDeployer;
import org.codehaus.cargo.util.CargoException;

/**
 * GlassFish 3.x installed local container.
 * 
 * @version $Id$
 */
public class GlassFish3xInstalledLocalContainer extends AbstractGlassFishInstalledLocalContainer
{

    /**
     * Container capability instance.
     */
    private static final ContainerCapability CAPABILITY = new GlassFish3xContainerCapability();

    /**
     * Calls parent constructor, which saves the configuration.
     *
     * @param localConfiguration Configuration.
     */
    public GlassFish3xInstalledLocalContainer(LocalConfiguration localConfiguration)
    {
        super(localConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invokeAsAdmin(boolean async, Java java, String[] args)
    {
        String home = this.getHome();
        if (home == null || !this.getFileHandler().isDirectory(home))
        {
            throw new CargoException("GlassFish home directory is not set");
        }

        File adminCli = new File(home, "glassfish/modules/admin-cli.jar");
        if (!adminCli.isFile())
        {
            throw new CargoException("Cannot find the GlassFish admin CLI JAR: "
                + adminCli.getName());
        }

        java.setJar(adminCli);
        for (String arg : args)
        {
            java.createArg().setValue(arg);
        }

        if (async)
        {
            AntContainerExecutorThread glassFishRunner = new AntContainerExecutorThread(java);
            glassFishRunner.start();
        }
        else
        {
            int exitCode = java.executeJava();

            if (exitCode != 0 && exitCode != 1)
            {
                throw new CargoException("Command " + args[0] + " failed: asadmin exited "
                    + exitCode);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractLocalDeployer getDeployer()
    {
        return new GlassFish3xInstalledLocalDeployer(this);
    }

    /**
     * {@inheritDoc}
     */
    public ContainerCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     */
    public String getId()
    {
        return "glassfish3x";
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return "GlassFish 3.x";
    }

}
