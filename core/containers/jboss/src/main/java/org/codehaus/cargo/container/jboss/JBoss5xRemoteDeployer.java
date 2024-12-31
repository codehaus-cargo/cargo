/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
import org.codehaus.cargo.container.internal.util.ResourceUtils;
import org.codehaus.cargo.container.jboss.deployable.JBossWAR;
import org.codehaus.cargo.container.jboss.internal.IJBossProfileManagerDeployer;
import org.codehaus.cargo.container.spi.deployer.AbstractRemoteDeployer;
import org.codehaus.cargo.util.CargoException;

/**
 * Remote deployer that uses the Profile Service to deploy to JBoss.
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

        URL deployerJarURL;
        try (InputStream deployerJarInputStream =
            this.getClass().getClassLoader().getResourceAsStream(deployerJarName))
        {
            if (deployerJarInputStream == null)
            {
                throw new CargoException("Cannot locate the JBoss deployer helper JAR, "
                    + "is the CARGO JBoss container JAR broken?");
            }
            try
            {
                File deployerJarFile = File.createTempFile("cargo-jboss-deployer-", ".jar");
                try (FileOutputStream deployerJarOutputStream =
                    new FileOutputStream(deployerJarFile))
                {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = deployerJarInputStream.read(buf)) > 0)
                    {
                        deployerJarOutputStream.write(buf, 0, len);
                    }
                    deployerJarURL = deployerJarFile.toURI().toURL();
                }
            }
            catch (IOException e)
            {
                throw new CargoException("Cannot create the JBoss remote deployer: "
                    + e.getMessage(), e);
            }
        }
        catch (IOException e)
        {
            throw new CargoException("Cannot create the JBoss remote deployer: "
                + e.getMessage(), e);
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
                + "required JBoss JARs (or associated Maven dependencies) are in Codehaus Cargo's "
                + "classpath.\nMore information on: "
                + "https://codehaus-cargo.github.io/cargo/JBoss+Remote+Deployer.html", e);
        }

        // CARGO-1546: Please do ignore the warning stating that there might be a resource leak
        // due to the fact that deployerClassLoader is never closed - It actually should not be
        // closed as long as we have the JBoss5xRemoteDeployer#deployer still active
        URLClassLoader deployerClassLoader =
            new URLClassLoader(new URL[] {deployerJarURL}, jBossConnectorClassLoader);
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
            try
            {
                deployerClassLoader.close();
                deployerClassLoader = null;
                System.gc();
            }
            catch (IOException ignored)
            {
                // Ignored
            }

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
     * Get the deployable name for a given deployable. This also takes into account the WAR
     * context, including the case where it would be the root WAR. Moreover, when the JBoss WAR
     * file has the context root set in the <code>jboss-web.xml</code> file, it will
     * <a href="https://codehaus-cargo.atlassian.net/browse/CARGO-1577">return the original WAR
     * file name</a>.
     * @param deployable Deployable to get the name for.
     * @return Name for <code>deployable</code>.
     */
    private String getDeployableName(Deployable deployable)
    {
        if (deployable instanceof JBossWAR)
        {
            JBossWAR jbossWar = (JBossWAR) deployable;
            if (jbossWar.containsJBossWebContext())
            {
                jbossWar.informJBossWebContext(getLogger());
                if ("true".equalsIgnoreCase(getContainer().getConfiguration().getPropertyValue(
                    JBossPropertySet.DEPLOYER_KEEP_ORIGINAL_WAR_FILENAME)))
                {
                    // CARGO-1577: When the JBoss or WildFly WAR file has the context root set
                    //             in the jboss-web.xml file, keep the original WAR file name
                    return jbossWar.getFileHandler().getName(jbossWar.getFile());
                }
            }
        }

        return deployable.getFilename();
    }
}
