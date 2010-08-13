/* 
 * ========================================================================
 *
 * Copyright 2005 Jeff Genender. Code from this file
 * was originally imported from the JBoss Maven2 plugin.
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
package org.codehaus.cargo.container.jboss;

import java.io.File;
import java.lang.reflect.Constructor;

import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.jboss.internal.IJBossProfileManagerDeployer;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.deployer.AbstractRemoteDeployer;
import org.codehaus.cargo.util.CargoException;

/**
 * Remote deployer that uses the Profile Service to deploy to JBoss.
 * 
 * @version $Id$
 */
public class JBoss5xRemoteDeployer extends AbstractRemoteDeployer
{

    /**
     * The deployer to use.
     */
    private IJBossProfileManagerDeployer deployer;

    /**
     * @param container the container containing the configuration to use to find the deployer
     *        properties such as url, user name and password to use to connect to the deployer
     */
    public JBoss5xRemoteDeployer(RemoteContainer container)
    {
        final String classToLoad = "org.codehaus.cargo.tools.jboss.JBossDeployer";

        StringBuilder providerURL = new StringBuilder();
        providerURL.append("jnp://");
        providerURL.append(container.getConfiguration().getPropertyValue(
            GeneralPropertySet.HOSTNAME));
        providerURL.append(':');
        providerURL.append(container.getConfiguration().getPropertyValue(
            GeneralPropertySet.RMI_PORT));

        try
        {
            Class<?> jbossDeployerClass = null;
            final ClassLoader tcccl = Thread.currentThread().getContextClassLoader();
            if (tcccl != null)
            {
                try
                {
                    jbossDeployerClass = tcccl.loadClass(classToLoad);
                }
                catch (ClassNotFoundException e)
                {
                    jbossDeployerClass = null;
                }
            }
            if (jbossDeployerClass == null)
            {
                jbossDeployerClass = this.getClass().getClassLoader().loadClass(classToLoad);
            }

            Constructor<?> jbossDeployerConstructor = jbossDeployerClass.getConstructor(
                String.class);
            this.deployer = (IJBossProfileManagerDeployer) jbossDeployerConstructor.newInstance(
                providerURL.toString());
        }
        catch (ClassNotFoundException e)
        {
            throw new CargoException(
                "Cannot locate the JBoss deployer class! Make sure the jboss-deployer for your\n"
                + "JBoss version as well as all required JBoss JARs are in CARGO's classpath.\n"
                + "More information on: http://cargo.codehaus.org/JBoss+Remote+Deployer", e);
        }
        catch (Throwable t)
        {
            throw new CargoException("Cannot create a JBoss deployer: " + t.getMessage(), t);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#deploy(Deployable)
     */
    @Override
    public void deploy(Deployable deployable)
    {
        File deployableFile = new File(deployable.getFile());
        try
        {
            this.deployer.deploy(deployableFile, deployableFile.getName());
        }
        catch (Throwable t)
        {
            throw new CargoException("Cannot deploy deployable " + deployable, t);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#undeploy(Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        File deployableFile = new File(deployable.getFile());
        try
        {
            this.deployer.undeploy(deployableFile.getName());
        }
        catch (Throwable t)
        {
            throw new CargoException("Cannot undeploy deployable " + deployable, t);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#redeploy(Deployable)
     */
    @Override
    public void redeploy(Deployable deployable)
    {
        try
        {
            this.undeploy(deployable);
        }
        catch (Throwable ignored)
        {
            // Ignored
        }

        this.deploy(deployable);
    }

}
