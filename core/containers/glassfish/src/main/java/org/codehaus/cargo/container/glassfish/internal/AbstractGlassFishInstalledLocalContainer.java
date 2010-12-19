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
package org.codehaus.cargo.container.glassfish.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import org.apache.tools.ant.taskdefs.Java;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.glassfish.GlassFishInstalledLocalDeployer;
import org.codehaus.cargo.container.glassfish.GlassFishPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.util.CargoException;

/**
 * Abstract GlassFish installed local container.
 * 
 * @version $Id$
 */
public abstract class AbstractGlassFishInstalledLocalContainer
    extends AbstractInstalledLocalContainer
{

    /**
     * Calls parent constructor, which saves the configuration.
     *
     * @param localConfiguration Configuration.
     */
    public AbstractGlassFishInstalledLocalContainer(LocalConfiguration localConfiguration)
    {
        super(localConfiguration);
    }

    /**
     * Invokes asadmin.
     *
     * @param async Asynchronous invoke?
     * @param args Invoke arguments.
     */
    public void invokeAsAdmin(boolean async, String[] args)
    {
        Java java = this.createJavaTask();
        invokeAsAdmin(async, java, args);
    }

    /**
     * Invokes asadmin using a Java container.
     *
     * @param async Asynchronous invoke?
     * @param java ANT Java container task.
     * @param args Invoke arguments.
     */
    public abstract void invokeAsAdmin(boolean async, Java java, String[] args);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStart(Java java) throws Exception
    {
        this.getConfiguration().configure(this);

        this.getLogger().debug("Starting domain on HTTP port "
            + this.getConfiguration().getPropertyValue(ServletPropertySet.PORT)
            + " and admin port "
            + this.getConfiguration().getPropertyValue(GlassFishPropertySet.ADMIN_PORT),
            this.getClass().getName());

        // see https://glassfish.dev.java.net/issues/show_bug.cgi?id=885
        // needs to spawn
        this.invokeAsAdmin(true, java, new String[]
        {
            "start-domain",
            "--interactive=false",
            "--domaindir",
            this.getConfiguration().getHome(),
            this.getConfiguration().getPropertyValue(GlassFishPropertySet.DOMAIN_NAME)
        });

        // wait for the server to start
        boolean started = false;
        URL adminURL = new URL("http://"
            + this.getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME) + ":"
            + this.getConfiguration().getPropertyValue(GlassFishPropertySet.ADMIN_PORT) + "/");
        long timeout = System.currentTimeMillis() + this.getTimeout();
        while (System.currentTimeMillis() < timeout)
        {
            try
            {
                InputStream is = adminURL.openConnection().getInputStream();
                is.close();
                started = true;
                break;
            }
            catch (IOException e)
            {
                // Keep on waiting
                Thread.sleep(1000);
            }
        }
        if (!started)
        {
            throw new CargoException("GlassFish server admin still not accessible on " + adminURL
                + " after " + this.getTimeout() + " milliseconds!");
        }

        GlassFishInstalledLocalDeployer deployer = new GlassFishInstalledLocalDeployer(this);
        // deploy scheduled deployables
        for (Iterator iterator = this.getConfiguration().getDeployables().iterator(); iterator
            .hasNext();)
        {
            deployer.deploy((Deployable) iterator.next());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStop(Java java) throws Exception
    {
        this.invokeAsAdmin(false, java, new String[]
        {
            "stop-domain",
            "--domaindir",
            this.getConfiguration().getHome(),
            this.getConfiguration().getPropertyValue(GlassFishPropertySet.DOMAIN_NAME)
        });
    }

}
