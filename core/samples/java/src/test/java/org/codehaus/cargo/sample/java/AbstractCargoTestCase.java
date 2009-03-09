/* 
 * ========================================================================
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
package org.codehaus.cargo.sample.java;

import junit.framework.TestCase;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.installer.Proxy;
import org.codehaus.cargo.container.installer.ZipURLInstaller;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.generic.deployer.DefaultDeployerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.util.log.FileLogger;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.LogLevel;

import java.io.File;

public class AbstractCargoTestCase extends TestCase
{
    private static final ContainerFactory CONTAINER_FACTORY = new DefaultContainerFactory();

    private static final ConfigurationFactory CONFIGURATION_FACTORY =
        new DefaultConfigurationFactory();

    private static final DeployerFactory DEPLOYER_FACTORY = new DefaultDeployerFactory();

    private Container container;

    /**
     * Test data depending on the user's environment.
     */
    private EnvironmentTestData testData;

    protected Logger logger;

    private ClassLoader classLoader;

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

    protected void setContainer(Container container)
    {
        this.container = container;
    }

    protected LocalContainer getLocalContainer()
    {
        return (LocalContainer) this.container;
    }

    protected InstalledLocalContainer getInstalledLocalContainer()
    {
        return (InstalledLocalContainer) this.container;
    }

    protected RemoteContainer getRemoteContainer()
    {
        return (RemoteContainer) this.container;
    }

    protected Container getContainer()
    {
        return this.container;
    }

    public Deployer createDeployer(DeployerType type, Container container)
    {
        Deployer deployer = DEPLOYER_FACTORY.createDeployer(container, type);
        deployer.setLogger(getLogger());

        return deployer;
    }

    public Configuration createConfiguration(ConfigurationType type)
    {
        return createConfiguration(type, getTestData().targetDir);
    }

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
        if (getTestData().javaHome != null && !getTestData().javaHome.equals(""))
        {
            configuration.setProperty(GeneralPropertySet.JAVA_HOME, getTestData().javaHome);
        }
        configuration.setProperty(GeneralPropertySet.LOGGING, "high");

        configuration.setLogger(getLogger());

        return configuration;
    }

    public Container createContainer(Configuration configuration)
    {
        return createContainer(getTestData().containerType, configuration);
    }

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
            setUpXercesIfJDK14(configuration, (InstalledLocalContainer) container);
            setUpXercesIfJDK15(configuration, (InstalledLocalContainer) container);
        }

        File logFile = new File(new File(getTestData().targetDir).getParentFile(), "output.log");
        // delete the previous run's output, if it exists.
        logFile.delete();
        container.setOutput(logFile.getPath());
        // if we don't set append, the stop command will truncate the logging made during start.
        container.setAppend(true);
        container.setTimeout(getTestData().containerTimeout);
    }

    /**
     * @return the test data such as the target directory where to install configurations, etc
     */
    protected EnvironmentTestData getTestData()
    {
        return this.testData;
    }

    protected Logger getLogger()
    {
        return this.logger;
    }

    public String getName()
    {
        return super.getName() + " (" + getTestData().containerId + ","
            + getTestData().containerType + ")";
    }

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

    protected void tearDown()
    {
        // Reset context classloader. See the comment in setUp().
        Thread.currentThread().setContextClassLoader(null);

        // Stop any local container that is still running
        if ((this.container != null) && this.container.getType().isLocal())
        {
            LocalContainer container = (LocalContainer) this.container;
            if ((container.getState().isStarted()) || (container.getState().isStarting()))
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
     * need a modern parser that may not be present in JDK 1.4
     */
    private void setUpXercesIfJDK14(Configuration configuration, InstalledLocalContainer container)
    {
        if (configuration.getPropertyValue(GeneralPropertySet.JAVA_HOME).equals(
            System.getProperty("cargo.java.home.1_4")))
        {

            String xerces = System.getProperty("cargo.testdata.xerces-jars");
            if (xerces != null)
            {
                String[] jars = container.getFileHandler().getChildren(xerces);
                for (int i = 0; i < jars.length; i++)
                {
                    container.addExtraClasspath(jars[i]);
                }
                container.getSystemProperties().put("javax.xml.parsers.SAXParserFactory",
                    "org.apache.xerces.jaxp.SAXParserFactoryImpl");
                container.getSystemProperties().put("javax.xml.parsers.DocumentBuilderFactory",
                    "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
            }

        }

    }

    /**
     * some old containers (orion2x) include old versions of crimson that cannot parse schema
     */
    private void setUpXercesIfJDK15(Configuration configuration, InstalledLocalContainer container)
    {
        if (configuration.getPropertyValue(GeneralPropertySet.JAVA_HOME).equals(
            System.getProperty("cargo.java.home.1_5")))
        {
            container.getSystemProperties().put("javax.xml.parsers.SAXParserFactory",
                "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
            container.getSystemProperties().put("javax.xml.parsers.DocumentBuilderFactory",
                "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
        }

    }

    /**
     * Use the home dir if specified by the user or download the container distribution and installs
     * it if an install URL has been specified.
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

    private String installContainer()
    {
        ZipURLInstaller installer =
            new ZipURLInstaller(getTestData().installURL, getTestData().installDir);
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

    private void loadEmbeddedContainerDependencies() throws Exception
    {
        if (testData.containerId.startsWith("jetty"))
        {
            if (isCustomContextClassLoaderLoaded())
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

    private boolean isCustomContextClassLoaderLoaded()
    {
        boolean found;

        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();

        try
        {
            currentClassLoader.loadClass("org.mortbay.jetty.Server");
            found = true;
        }
        catch (Exception e)
        {
            found = false;
        }

        return found;
    }
}
