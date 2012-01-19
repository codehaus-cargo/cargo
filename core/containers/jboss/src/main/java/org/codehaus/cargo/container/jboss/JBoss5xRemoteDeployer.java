/* 
 * ========================================================================
 *
 * Copyright 2005 Jeff Genender. Code from this file
 * was originally imported from the JBoss Maven2 plugin.
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
package org.codehaus.cargo.container.jboss;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;

import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.internal.util.ResourceUtils;
import org.codehaus.cargo.container.jboss.internal.IJBossProfileManagerDeployer;
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
     * properties such as url, user name and password to use to connect to the deployer
     */
    public JBoss5xRemoteDeployer(RemoteContainer container)
    {
        super(container);

        String deployerJarName = "org/codehaus/cargo/container/jboss/deployer/"
            + getJBossRemoteDeployerJarName() + ".jar";

        InputStream deployerJarInputStream =
            this.getClass().getClassLoader().getResourceAsStream(deployerJarName);
        if (deployerJarInputStream == null)
        {
            throw new CargoException("Cannot locate the JBoss deployer helper JAR, "
                + "is the CARGO JBoss container JAR broken?");
        }
        URL deployerJarURL;
        FileOutputStream deployerJarOutputStream = null;
        try
        {
            File deployerJarFile = File.createTempFile("cargo-jboss-deployer-", ".jar");
            deployerJarOutputStream = new FileOutputStream(deployerJarFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = deployerJarInputStream.read(buf)) > 0)
            {
                deployerJarOutputStream.write(buf, 0, len);
            }
            deployerJarURL = deployerJarFile.toURI().toURL();
        }
        catch (IOException e)
        {
            throw new CargoException("Cannot create the JBoss remote deployer: "
                + e.getMessage(), e);
        }
        finally
        {
            try
            {
                deployerJarInputStream.close();
            }
            catch (IOException e)
            {
                // Ignored
            }

            if (deployerJarOutputStream != null)
            {
                try
                {
                    deployerJarOutputStream.close();
                }
                catch (IOException e)
                {
                    // Ignored
                }
            }

            deployerJarInputStream = null;
            deployerJarOutputStream = null;
            System.gc();
        }

        ClassLoader jBossConnectorClassLoader = null;
        try
        {
            String jBossConnectorClassName = getJBossConnectorClassName();

            try
            {
                ResourceUtils.getResourceLoader().loadClass(jBossConnectorClassName);
                jBossConnectorClassLoader = ResourceUtils.getResourceLoader();
            }
            catch (ClassNotFoundException e)
            {
                // Never mind, we'll look for this class elsewhere
            }

            if (jBossConnectorClassLoader == null)
            {
                final ClassLoader tcccl = Thread.currentThread().getContextClassLoader();
                if (tcccl != null)
                {
                    try
                    {
                        tcccl.loadClass(jBossConnectorClassName);
                        jBossConnectorClassLoader = tcccl;
                    }
                    catch (ClassNotFoundException e)
                    {
                        // Never mind, we'll look for this class elsewhere
                    }
                }
            }

            if (jBossConnectorClassLoader == null)
            {
                jBossConnectorClassLoader = this.getClass().getClassLoader();
                jBossConnectorClassLoader.loadClass(jBossConnectorClassName);
            }
        }
        catch (ClassNotFoundException e)
        {
            throw new CargoException("Cannot locate the JBoss connector classes! Make sure the "
                + "required JBoss JARs (or Maven dependencies) are in CARGO's classpath.\n"
                + "More information on: http://cargo.codehaus.org/JBoss+Remote+Deployer", e);
        }

        URL[] deployerJarURLArray = new URL[] {deployerJarURL};
        ClassLoader deployerClassLoader =
            new URLClassLoader(deployerJarURLArray, jBossConnectorClassLoader);
        try
        {
            final String classToLoad = "org.codehaus.cargo.tools.jboss.JBossDeployer";
            Class<?> jbossDeployerClass = deployerClassLoader.loadClass(classToLoad);

            Constructor<?> jbossDeployerConstructor = jbossDeployerClass.getConstructor(
                Configuration.class);
            this.deployer = (IJBossProfileManagerDeployer)
                jbossDeployerConstructor.newInstance(container.getConfiguration());
        }
        catch (Throwable t)
        {
            throw new CargoException("Cannot create the JBoss remote deployer: "
                + t.getMessage(), t);
        }
    }

    /**
     * @return The JAR name to load for the JBoss remote deployer.
     */
    protected String getJBossRemoteDeployerJarName()
    {
        return "jboss-deployer-5";
    }

    /**
     * @return The class name to load for the JBoss JNDI context.
     */
    protected String getJBossConnectorClassName()
    {
        return "org.jnp.interfaces.NamingContextFactory";
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
            this.deployer.deploy(deployableFile, getDeployableName(deployable));
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
        try
        {
            this.deployer.undeploy(getDeployableName(deployable));
        }
        catch (Throwable t)
        {
            throw new CargoException("Cannot undeploy deployable " + deployable, t);
        }
    }

    /**
     * Get the deployable name for a given deployable. This also takes into account the WAR context.
     * @param deployable Deployable to get the name for.
     * @return Name for <code>deployable</code>.
     */
    private String getDeployableName(Deployable deployable)
    {
        File localFile = new File(deployable.getFile());
        String localFileName = localFile.getName();
        if (deployable.getType() == DeployableType.WAR)
        {
            WAR war = (WAR) deployable;
            if (war.getContext().length() == 0)
            {
                localFileName = "rootContext.war";
            }
            else
            {
                localFileName = war.getContext() + ".war";
            }
        }

        return localFileName;
    }
}
