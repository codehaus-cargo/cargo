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
package org.codehaus.cargo.sample.java;

import java.io.File;
import java.util.Map.Entry;
import java.util.Properties;

import junit.framework.TestCase;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.installer.Proxy;
import org.codehaus.cargo.container.installer.ZipURLInstaller;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.LoggingLevel;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.generic.deployer.DefaultDeployerFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.util.log.FileLogger;
import org.codehaus.cargo.util.log.LogLevel;
import org.codehaus.cargo.util.log.Logger;

/**
 * Abstract test case for Cargo samples.
 * 
 * @version $Id$
 */
public class AbstractCargoTestCase extends TestCase
{
    /**
     * Container factory.
     */
    private static final ContainerFactory CONTAINER_FACTORY = new DefaultContainerFactory();

    /**
     * Configuration factory.
     */
    private static final ConfigurationFactory CONFIGURATION_FACTORY =
        new DefaultConfigurationFactory();

    /**
     * Deployer factory.
     */
    private static final DeployerFactory DEPLOYER_FACTORY = new DefaultDeployerFactory();

    /**
     * Container that's currently being tested.
     */
    private Container container;

    /**
     * Test data depending on the user's environment.
     */
    private EnvironmentTestData testData;

    /**
     * Logger.
     */
    private Logger logger;

    /**
     * Class loader, used for embedded containers.
     */
    private ClassLoader classLoader;

    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public AbstractCargoTestCase(String testName, EnvironmentTestData testData) throws Exception
    {
        super(testName);

        // Save the testData for use by TestCases extending this class
        this.testData = testData;

        // Ensure target dir exists so that we can create the log file
        new File(getTestData().targetDir).mkdirs();

        this.logger =
            new FileLogger(new File(new File(getTestData().targetDir).getParentFile(),
                "cargo.log"), true);
        this.logger.setLevel(LogLevel.DEBUG);
    }

    /**
     * @param container Container to set.
     */
    protected void setContainer(Container container)
    {
        this.container = container;
    }

    /**
     * @return Container cast as {@link LocalContainer}.
     */
    protected LocalContainer getLocalContainer()
    {
        return (LocalContainer) this.container;
    }

    /**
     * @return Container cast as {@link InstalledLocalContainer}.
     */
    protected InstalledLocalContainer getInstalledLocalContainer()
    {
        return (InstalledLocalContainer) this.container;
    }

    /**
     * @return Container cast as {@link RemoteContainer}.
     */
    protected RemoteContainer getRemoteContainer()
    {
        return (RemoteContainer) this.container;
    }

    /**
     * @return Container as it is.
     */
    protected Container getContainer()
    {
        return this.container;
    }

    /**
     * Creates a deployer.
     * @param type Deployer type.
     * @param container Container to create a deployer for.
     * @return {@link Deployer} instance for the given {@link Container} and {@link DeployerType}.
     */
    public Deployer createDeployer(DeployerType type, Container container)
    {
        Deployer deployer = DEPLOYER_FACTORY.createDeployer(container, type);
        deployer.setLogger(getLogger());

        return deployer;
    }

    /**
     * Creates a deployer.
     * @param container Container to create a deployer for.
     * @return {@link Deployer} instance for the given {@link Container}.
     */
    public Deployer createDeployer(Container container)
    {
        Deployer deployer = DEPLOYER_FACTORY.createDeployer(container);
        deployer.setLogger(getLogger());

        return deployer;
    }

    /**
     * Creates a configuration.
     * @param type Configuration type.
     * @return {@link Configuration} instance for the given {@link ConfigurationType}.
     */
    public Configuration createConfiguration(ConfigurationType type)
    {
        return createConfiguration(type, getTestData().targetDir);
    }

    /**
     * Creates a configuration.
     * @param type Configuration type.
     * @param targetDir Target directory.
     * @return {@link Configuration} instance for the given {@link ConfigurationType} created in
     * the given directory.
     */
    public Configuration createConfiguration(ConfigurationType type, String targetDir)
    {
        Configuration configuration;

        if (type != ConfigurationType.RUNTIME)
        {
            configuration =
                CONFIGURATION_FACTORY.createConfiguration(getTestData().containerId,
                    getTestData().containerType, type, targetDir);
        }
        else
        {
            configuration =
                CONFIGURATION_FACTORY.createConfiguration(getTestData().containerId,
                    getTestData().containerType, type);
        }

        configuration.setProperty(ServletPropertySet.PORT, "" + getTestData().port);
        configuration.setProperty(GeneralPropertySet.RMI_PORT, "" + getTestData().rmiPort);
        Properties allProperties = System.getProperties();
        for (Entry<Object, Object> property : allProperties.entrySet())
        {
            if (property.getKey() != null && property.getValue() != null)
            {
                String key = property.getKey().toString();
                String value = property.getValue().toString();
                String containerIdentifier = "cargo.samples." + getTestData().containerId;

                if (key.startsWith(containerIdentifier))
                {
                    key = key.replace(containerIdentifier, "cargo");
                }
                configuration.setProperty(key, value);
            }
        }
        if (getTestData().javaHome != null && !getTestData().javaHome.equals(""))
        {
            configuration.setProperty(GeneralPropertySet.JAVA_HOME, getTestData().javaHome);
        }
        configuration.setProperty(GeneralPropertySet.LOGGING, LoggingLevel.HIGH.getLevel());

        configuration.setLogger(getLogger());

        return configuration;
    }

    /**
     * Creates a container for a given configuration.
     * @param configuration Container configuration.
     * @return {@link Container} for the given {@link Configuration}.
     */
    public Container createContainer(Configuration configuration)
    {
        return createContainer(getTestData().containerType, configuration);
    }

    /**
     * Creates a container of a given type for a given configuration.
     * @param type Container type.
     * @param configuration Container configuration.
     * @return {@link Container} of a given {@link ContainerType} for the given
     * {@link Configuration}.
     */
    public Container createContainer(ContainerType type, Configuration configuration)
    {
        Container container =
            CONTAINER_FACTORY.createContainer(getTestData().containerId, type, configuration);

        container.setLogger(getLogger());

        // Set up local container-specific settings
        if (container.getType().isLocal())
        {
            setUpLocalSettings(configuration, (LocalContainer) container);
        }

        return container;
    }

    /**
     * Setup local settings.
     * @param configuration Container configuration.
     * @param container Local container.
     */
    private void setUpLocalSettings(Configuration configuration, LocalContainer container)
    {
        if (container.getType() == ContainerType.EMBEDDED)
        {
            ((EmbeddedLocalContainer) container).setClassLoader(this.classLoader);
        }
        else if (container.getType() == ContainerType.INSTALLED)
        {
            setUpHome((InstalledLocalContainer) container);
            setUpClover((InstalledLocalContainer) container);
        }

        File logFile = new File(new File(getTestData().targetDir).getParentFile(), "output.log");
        // delete the previous run's output, if it exists.
        logFile.delete();
        container.setOutput(logFile.getPath());
        container.setTimeout(getTestData().containerTimeout);
    }

    /**
     * @return the test data such as the target directory where to install configurations, etc.
     */
    protected EnvironmentTestData getTestData()
    {
        return this.testData;
    }

    /**
     * @return logger.
     */
    protected Logger getLogger()
    {
        return this.logger;
    }

    /**
     * {@inheritDoc}
     * @return nicely formatted test name (with container id and type).
     */
    @Override
    public String getName()
    {
        return super.getName() + " (" + getTestData().containerId + ","
            + getTestData().containerType + ")";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception
    {
        getLogger().info("Starting test [" + getName() + "]", this.getClass().getName());

        // Set up the thread context classloader for embedded containers. We're doing this here
        // instead of in the constructor as this needs to be set for each single test as otherwise
        // the diferent embedded containers will conflict with each other.
        // TODO: Modify the Jetty embedded container implementation so that this is done at each
        // method invoked using reflection, as it's done for the Tomcat 5.x embedded implementation.
        if (getTestData().containerType == ContainerType.EMBEDDED)
        {
            loadEmbeddedContainerDependencies();
        }
    }

    @Override
    protected void tearDown()
    {
        // Reset context classloader. See the comment in setUp().
        Thread.currentThread().setContextClassLoader(null);

        // Stop any local container that is still running
        if (this.container != null && this.container.getType().isLocal())
        {
            LocalContainer container = (LocalContainer) this.container;
            if (container.getState().isStarted() || container.getState().isStarting())
            {
                getLogger().info(
                    "Container is in the [" + container.getState() + "] state"
                        + ", shutting it down now", this.getClass().getName());
                container.stop();
            }
        }
        getLogger().info("Ending test [" + getName() + "]", this.getClass().getName());
    }

    /**
     * Add the Clover jar to the container classpath to support Clovering the tests and set up the
     * Clover license.
     * @param container Container to add Clover JAR into.
     */
    private void setUpClover(InstalledLocalContainer container)
    {
        if (System.getProperty("cargo.clover.jar") != null)
        {
            container.addExtraClasspath(System.getProperty("cargo.clover.jar"));
            if (System.getProperty("cargo.clover.license") != null)
            {
                System.setProperty("clover.license.path", System
                    .getProperty("cargo.clover.license"));
            }
        }
    }

    /**
     * Use the home dir if specified by the user or download the container distribution and installs
     * it if an install URL has been specified.
     * @param container Container to configure with the extracted home.
     */
    private void setUpHome(InstalledLocalContainer container)
    {
        if (getTestData().home != null)
        {
            container.setHome(getTestData().home);
        }
        else if (getTestData().installURL != null)
        {
            container.setHome(installContainer());
        }
    }

    /**
     * Install container using {@link ZipURLInstaller}.
     * @return Location in which the container has been installed.
     */
    private String installContainer()
    {
        ZipURLInstaller installer = new ZipURLInstaller(getTestData().installURL,
            getTestData().downloadDir, getTestData().extractDir);
        installer.setLogger(getLogger());

        // Set up proxy
        if (getTestData().proxy != null)
        {
            Proxy userProxy = getTestData().proxy;
            userProxy.setLogger(getLogger());
            installer.setProxy(userProxy);
        }
        installer.install();

        return installer.getHome();
    }

    /**
     * Load embedded container dependencies.
     * @throws Exception If anything goes wrong.
     */
    private void loadEmbeddedContainerDependencies() throws Exception
    {
        if (testData.containerId.startsWith("jetty"))
        {
            boolean found;

            try
            {
                ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
                currentClassLoader.loadClass("org.mortbay.jetty.Server");
                found = true;
            }
            catch (ClassNotFoundException e)
            {
                found = false;
            }

            if (found)
            {
                this.classLoader = Thread.currentThread().getContextClassLoader();
                return;
            }
        }

        // Otherwise create a classloader and load them externally
        EmbeddedContainerClasspathResolver resolver = new EmbeddedContainerClasspathResolver();
        this.classLoader = resolver.resolveDependencies(testData.containerId, installContainer());
        if (this.classLoader != null)
        {
            Thread.currentThread().setContextClassLoader(this.classLoader);
        }
    }
}
