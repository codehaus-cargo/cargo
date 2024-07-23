/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.documentation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.bsc.confluence.ConfluenceService;
import org.bsc.confluence.ConfluenceServiceFactory;
import org.bsc.confluence.ConfluenceService.Credentials;
import org.bsc.confluence.ConfluenceService.Storage;
import org.bsc.confluence.ConfluenceService.Model.Page;
import org.bsc.confluence.ConfluenceService.Storage.Representation;
import org.bsc.mojo.configuration.ScrollVersionsInfo;
import org.bsc.ssl.SSLCertificateInfo;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.geronimo.GeronimoPropertySet;
import org.codehaus.cargo.container.glassfish.GlassFishPropertySet;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.jetty.JettyPropertySet;
import org.codehaus.cargo.container.jonas.JonasPropertySet;
import org.codehaus.cargo.container.jrun.JRun4xPropertySet;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ResourcePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.resin.ResinPropertySet;
import org.codehaus.cargo.container.tomcat.TomcatPropertySet;
import org.codehaus.cargo.container.tomee.TomeePropertySet;
import org.codehaus.cargo.container.weblogic.WebLogicPropertySet;
import org.codehaus.cargo.container.websphere.WebSpherePropertySet;
import org.codehaus.cargo.container.wildfly.WildFlyPropertySet;
import org.codehaus.cargo.generic.ContainerCapabilityFactory;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.DefaultContainerCapabilityFactory;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.generic.deployer.DefaultDeployerFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.sample.java.CargoTestSuite;
import org.codehaus.cargo.sample.java.EnvironmentTestData;
import org.codehaus.cargo.sample.java.JmsQueueResourceOnStandaloneConfigurationTest;
import org.codehaus.cargo.sample.java.JmsTopicResourceOnStandaloneConfigurationTest;
import org.codehaus.cargo.sample.java.MailResourceOnStandaloneConfigurationTest;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Generate container documentation using Confluence markup language. The generated text is meant to
 * be copied on the Cargo Confluence web site.
 */
public class ConfluenceContainerDocumentationGenerator
{
    /**
     * Containers that only work with Java up to version 7 due to JSP issues.
     */
    private static final List<String> JAVA7_MAX_CONTAINERS_JSP = Arrays.asList(new String[]
    {
        "geronimo2x",
        "jboss5x",
        "jboss51x",
        "jboss6x",
        "jboss61x",
        "jonas4x",
        "tomcat5x"
    });

    /**
     * Containers that only work with Java up to version 7 due to OSGi issues.
     */
    private static final List<String> JAVA7_MAX_CONTAINERS_OSGI = Arrays.asList(new String[]
    {
        "geronimo3x",
        "glassfish3x",
        "jboss7x",
        "jboss71x",
        "jboss72x",
        "jboss73x"
    });

    /**
     * Names of Jakarta EE containers, i.e. starting from which version a container has moved
     * over to Jakarta EE.
     */
    private static final Map<String, String> JAKARTAEE_CONTAINER_NAMES =
        new HashMap<String, String>();

    /**
     * Initialize Jakarta EE container names.
     */
    static
    {
        JAKARTAEE_CONTAINER_NAMES.put("jetty", "Jetty 11");
        JAKARTAEE_CONTAINER_NAMES.put("glassfish", "GlassFish 6");
        JAKARTAEE_CONTAINER_NAMES.put("payara", "Payara 6");
        JAKARTAEE_CONTAINER_NAMES.put("tomcat", "Tomcat 10");
        JAKARTAEE_CONTAINER_NAMES.put("tomee", "TomEE 9");
        JAKARTAEE_CONTAINER_NAMES.put("wildfly", "WildFly 27");
    }

    /**
     * Classes that are used to get the property names.
     */
    private static final Class[] PROPERTY_SET_CLASSES =
    {
        DatasourcePropertySet.class,
        GeneralPropertySet.class,
        RemotePropertySet.class,
        ResourcePropertySet.class,
        ServletPropertySet.class,

        GeronimoPropertySet.class,
        GlassFishPropertySet.class,
        JBossPropertySet.class,
        JettyPropertySet.class,
        JonasPropertySet.class,
        JRun4xPropertySet.class,
        ResinPropertySet.class,
        TomcatPropertySet.class,
        TomeePropertySet.class,
        WebLogicPropertySet.class,
        WebSpherePropertySet.class,
        WildFlyPropertySet.class
    };

    /**
     * Prefix for all datasource-related properties.
     */
    private static final String DATASOURCE_PREFIX = "cargo.datasource.";

    /**
     * Prefix for all Javadoc URLs.
     */
    private static final String JAVADOC_URL_PREFIX = "https://codehaus-cargo.github.io/apidocs/";

    /**
     * The MavenXpp3Reader used to get project info from pom files.
     */
    private static final MavenXpp3Reader POM_READER = new MavenXpp3Reader();

    /**
     * Relative path to the cargo-core-samples directory.
     */
    private static final String SAMPLES_DIRECTORY = System.getProperty("basedir") + "/../samples/";

    /**
     * Relative path to the cargo-root directory.
     */
    private static final String CARGO_ROOT_DIRECTORY = System.getProperty("basedir") + "/../../";

    /**
     * Constant for known POM file name.
     */
    private static final String POM = "pom.xml";

    /**
     * Constant for the Surefire plugin name.
     */
    private static final String SUREFIRE_PLUGIN = "org.apache.maven.plugins:maven-surefire-plugin";

    /**
     * Constant for the systemPropertyVariables node of the Surefire plugin.
     */
    private static final String SYSTEM_PROPERTY_VARIABLES = "systemPropertyVariables";

    /**
     * Container factory.
     */
    private ContainerFactory containerFactory = new DefaultContainerFactory();

    /**
     * Configuration factory.
     */
    private ConfigurationFactory configurationFactory = new DefaultConfigurationFactory();

    /**
     * Deployer factory.
     */
    private DeployerFactory deployerFactory = new DefaultDeployerFactory();

    /**
     * Container capability factory.
     */
    private ContainerCapabilityFactory containerCapabilityFactory =
        new DefaultContainerCapabilityFactory();

    /**
     * Configuration capability factory.
     */
    private ConfigurationCapabilityFactory configurationCapabilityFactory =
        new DefaultConfigurationCapabilityFactory();

    /**
     * If configured, the Confluence service for updating the Wiki.
     */
    private ConfluenceService confluence = null;

    /**
     * Initialize the Confluence service, if necessary.
     * @throws Exception If reading the Confluence service credentials fails.
     */
    public ConfluenceContainerDocumentationGenerator() throws Exception
    {
        String confluenceCredentialsPath = System.getProperty("cargo.confluenceCredentialsPath");
        if (confluenceCredentialsPath != null && confluenceCredentialsPath.trim().length() > 0)
        {
            try (InputStream input = new FileInputStream(confluenceCredentialsPath.trim()))
            {
                Properties confluenceCredentials = new Properties();
                confluenceCredentials.load(input);
                Credentials credentials = new Credentials(
                    confluenceCredentials.getProperty("cargo.confluence.username"),
                        confluenceCredentials.getProperty("cargo.confluence.apiKey"));
                confluence = ConfluenceServiceFactory.createInstance(
                    "https://codehaus-cargo.atlassian.net/wiki/rest/api", credentials,
                        null, new SSLCertificateInfo(), new ScrollVersionsInfo());
            }
        }
    }

    /**
     * Generate documentation for a datasources.
     * @return Generated documentation.
     * @throws Exception If anything goes wrong.
     */
    public String generateDatasourceDocumentation() throws Exception
    {
        StringBuilder output = new StringBuilder();

        output.append("{note}This page / section has been automatically generated by Cargo's "
            + "build. Do not edit it directly as it'll be overwritten next time it's generated "
            + "again.{note}");
        output.append(FileHandler.NEW_LINE);
        output.append(FileHandler.NEW_LINE);

        output.append("|| Container || Configuration || Resource || DataSource || "
            + "Transactional DataSource || XA DataSource ||");
        output.append(FileHandler.NEW_LINE);

        Map<String, Set<ContainerType>> containerIds = containerFactory.getContainerIds();
        SortedMap<String, InstalledLocalContainer> sortedContainers =
            new TreeMap<String, InstalledLocalContainer>();
        for (String containerId : containerIds.keySet())
        {
            Map<String, Boolean> properties;
            try
            {
                properties = this.configurationCapabilityFactory.
                    createConfigurationCapability(containerId, ContainerType.INSTALLED,
                        ConfigurationType.STANDALONE).getProperties();
            }
            catch (ContainerException e)
            {
                // That container doesn't have an installed standalone configuration
                continue;
            }

            for (String property : properties.keySet())
            {
                if (property.startsWith(DATASOURCE_PREFIX)
                    || ResourcePropertySet.RESOURCE.equals(property))
                {
                    Configuration configuration = this.configurationFactory.createConfiguration(
                            containerId, ContainerType.INSTALLED, ConfigurationType.STANDALONE);
                    InstalledLocalContainer container = (InstalledLocalContainer)
                        this.containerFactory.createContainer(
                            containerId, ContainerType.INSTALLED, configuration);

                    String[] containerNameAndVersion = container.getName().toLowerCase(
                        Locale.ENGLISH).split("\\s");
                    if (containerNameAndVersion.length > 1)
                    {
                        if (containerNameAndVersion[1].charAt(0) >= '0'
                            && containerNameAndVersion[1].charAt(0) <= '9')
                        {
                            Double containerVersion = Double.parseDouble(
                                containerNameAndVersion[1].replace(".x", ""));
                            if (containerVersion < 10.0)
                            {
                                sortedContainers.put(
                                    containerNameAndVersion[0] + '0' + containerVersion,
                                        container);
                            }
                            else
                            {
                                sortedContainers.put(
                                    containerNameAndVersion[0] + containerVersion, container);
                            }
                        }
                        else
                        {
                            sortedContainers.put(
                                containerNameAndVersion[0] + containerNameAndVersion[1],
                                    container);
                        }
                    }
                    else
                    {
                        sortedContainers.put(containerNameAndVersion[0], container);
                    }
                }
            }
        }
        for (InstalledLocalContainer container : sortedContainers.values())
        {
            Configuration configuration = container.getConfiguration();
            Class configurationClass = configuration.getClass();
            Map<String, Boolean> properties = configuration.getCapability().getProperties();

            output.append("| [");
            output.append(container.getName());
            int bracket = container.getName().indexOf('(');
            if (bracket != -1)
            {
                output.append('|');
                output.append(container.getName().substring(0, bracket).trim());
            }
            output.append("] | {{");
            output.append(computedFQCN(configurationClass.getName()));
            output.append("}} | (");
            if (properties.keySet().contains(ResourcePropertySet.RESOURCE))
            {
                output.append("/)");
                String containerId = System.getProperty(
                    CargoTestSuite.SYSTEM_PROPERTY_CONTAINER_IDS);
                String deployables = System.getProperty("cargo.testdata.deployables");
                String targetDir = System.getProperty("cargo.target.dir");
                try
                {
                    File dummy = new File(System.getProperty("java.io.tmpdir"), "cargo-dummy");
                    dummy.mkdirs();
                    dummy.deleteOnExit();
                    File dummyDeployables = new File(dummy, "deployables");
                    dummyDeployables.mkdirs();
                    dummyDeployables.deleteOnExit();
                    File dummyJakartaEeDeployables = new File(dummy, "deployables-jakarta-ee");
                    dummyJakartaEeDeployables.mkdirs();
                    dummyJakartaEeDeployables.deleteOnExit();

                    System.setProperty(
                        CargoTestSuite.SYSTEM_PROPERTY_CONTAINER_IDS, container.getId());
                    System.setProperty(
                        "cargo.testdata.deployables", dummyDeployables.getAbsolutePath());
                    System.setProperty("cargo.target.dir", System.getProperty("java.io.tmpdir"));
                    if (JmsQueueResourceOnStandaloneConfigurationTest.suite().countTestCases() == 0)
                    {
                        output.append("^Q^");
                    }
                    if (JmsTopicResourceOnStandaloneConfigurationTest.suite().countTestCases() == 0)
                    {
                        output.append("^T^");
                    }
                    if (MailResourceOnStandaloneConfigurationTest.suite().countTestCases() == 0)
                    {
                        output.append("^M^");
                    }
                }
                finally
                {
                    if (containerId == null)
                    {
                        System.clearProperty(CargoTestSuite.SYSTEM_PROPERTY_CONTAINER_IDS);
                    }
                    else
                    {
                        System.setProperty(
                            CargoTestSuite.SYSTEM_PROPERTY_CONTAINER_IDS, containerId);
                    }
                    if (deployables == null)
                    {
                        System.clearProperty("cargo.testdata.deployables");
                    }
                    else
                    {
                        System.setProperty("cargo.testdata.deployables", deployables);
                    }
                    if (targetDir == null)
                    {
                        System.clearProperty("cargo.target.dir");
                    }
                    else
                    {
                        System.setProperty("cargo.target.dir", targetDir);
                    }
                }
            }
            else
            {
                output.append("x)");
            }
            output.append(" | (");
            if (properties.keySet().contains(DatasourcePropertySet.DATASOURCE))
            {
                output.append('/');
            }
            else
            {
                output.append('x');
            }
            output.append(") | (");
            if (properties.keySet().contains(DatasourcePropertySet.TRANSACTION_SUPPORT))
            {
                output.append('/');
            }
            else
            {
                output.append('x');
            }
            output.append(") | (");
            if (properties.keySet().contains(DatasourcePropertySet.CONNECTION_TYPE))
            {
                output.append('/');
            }
            else
            {
                output.append('x');
            }
            output.append(") |");
            output.append(FileHandler.NEW_LINE);
        }

        output.append(FileHandler.NEW_LINE);
        output.append("Container-specific notes:");
        output.append(FileHandler.NEW_LINE);
        output.append("* ^Q^: JMS Queues are not supported by this container");
        output.append(FileHandler.NEW_LINE);
        output.append("* ^T^: JMS Topics are not supported by this container");
        output.append(FileHandler.NEW_LINE);
        output.append("* ^M^: Mail resources are not supported by this container");
        output.append(FileHandler.NEW_LINE);

        if (confluence != null)
        {
            Page page = confluence.getPage(
                "CARGO", "Containers with DataSource and Resource support").get().get();
            confluence.storePage(page, Storage.of(output.toString(), Representation.WIKI));
        }

        return output.toString();
    }

    /**
     * Generate documentation for a given container.
     * @param containerId Container id.
     * @return Generated documentation.
     * @throws Exception If anything goes wrong.
     */
    public String generateDocumentation(String containerId) throws Exception
    {
        StringBuilder output = new StringBuilder();

        output.append("{note}This page has been automatically generated by Cargo's build. "
            + "Do not edit it directly as it'll be overwritten next time it's generated again."
            + "{note}");
        output.append(FileHandler.NEW_LINE);
        output.append(FileHandler.NEW_LINE);

        if (containerId.equals("geronimo1x"))
        {
            output.append(
                "{note}The Codehaus Cargo Geronimo 1.x container REQUIRES Geronimo 1.1.1.{note}");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
        }

        // See https://access.redhat.com/solutions/21906 for the exact versions
        if (containerId.startsWith("jboss6") || containerId.startsWith("jboss7")
            || containerId.equals("wildfly10x") || containerId.equals("wildfly11x")
            || containerId.equals("wildfly14x") || containerId.equals("wildfly18x")
            || containerId.equals("wildfly23x") || containerId.equals("wildfly28x"))
        {
            output.append("{info}With the opening of the JBoss EAP to the public and the split ");
            output.append("between JBoss and WildFly, the below naming correspondence should be ");
            output.append("used with JBoss EAP containers:");
            output.append(FileHandler.NEW_LINE);
            output.append("* What Cargo calls [JBoss 6.x] is what JBoss refers to as JBoss ");
            output.append("Application Server version 6; i.e. the version released in December ");
            output.append("2010");
            output.append(FileHandler.NEW_LINE);
            output.append("* What Cargo calls [JBoss 6.1.x] is what JBoss refers to as JBoss ");
            output.append("Application Server version 6.1; i.e. the version released in August ");
            output.append("2011");
            output.append(FileHandler.NEW_LINE);
            output.append("* What Cargo calls [JBoss 7.1.x] is what JBoss refers to as JBoss ");
            output.append("Enterprise Application Platform (EAP) version 6.0; i.e. the build ");
            output.append("from JBoss Application Server (AS) version 7.1 released in February ");
            output.append("2012");
            output.append(FileHandler.NEW_LINE);
            output.append("* What Cargo calls [JBoss 7.2.x] is what JBoss refers to as JBoss ");
            output.append("Enterprise Application Platform (EAP) version 6.1; i.e. the build ");
            output.append("from JBoss Application Server (AS) version 7.2 released in May 2013");
            output.append(FileHandler.NEW_LINE);
            output.append("* What Cargo calls [JBoss 7.3.x] is what JBoss refers to as JBoss ");
            output.append("Enterprise Application Platform (EAP) version 6.2; i.e. the build ");
            output.append("from JBoss Application Server (AS) version 7.3 released in October ");
            output.append("2013");
            output.append(FileHandler.NEW_LINE);
            output.append("* What Cargo calls [JBoss 7.4.x] is what JBoss refers to as JBoss ");
            output.append("Enterprise Application Platform (EAP) version 6.3; i.e. the build ");
            output.append("from JBoss Application Server (AS) version 7.4 released in June ");
            output.append("2014");
            output.append(FileHandler.NEW_LINE);
            output.append("* What Cargo calls [JBoss 7.5.x] is what JBoss refers to as JBoss ");
            output.append("Enterprise Application Platform (EAP) version 6.4; i.e. the build ");
            output.append("from JBoss Application Server (AS) version 7.5 released in October ");
            output.append("2015");
            output.append(FileHandler.NEW_LINE);
            output.append("* The [WildFly 10.x] container can be used with the JBoss Enterprise ");
            output.append("Application Platform (EAP) version 7.0; i.e. the version released in ");
            output.append("May 2016");
            output.append(FileHandler.NEW_LINE);
            output.append("* The [WildFly 11.x] container can be used with the JBoss Enterprise ");
            output.append("Application Platform (EAP) version 7.1; i.e. the version released in ");
            output.append("December 2017");
            output.append(FileHandler.NEW_LINE);
            output.append("* The [WildFly 14.x] container can be used with the JBoss Enterprise ");
            output.append("Application Platform (EAP) version 7.2; i.e. the version released in ");
            output.append("January 2019");
            output.append(FileHandler.NEW_LINE);
            output.append("* The [WildFly 18.x] container can be used with the JBoss Enterprise ");
            output.append("Application Platform (EAP) version 7.3; i.e. the version released in ");
            output.append("March 2020");
            output.append(FileHandler.NEW_LINE);
            output.append("* The [WildFly 23.x] container can be used with the JBoss Enterprise ");
            output.append("Application Platform (EAP) version 7.4; i.e. the version released in ");
            output.append("July 2021");
            output.append(FileHandler.NEW_LINE);
            output.append("* The [WildFly 28.x] container can be used with the JBoss Enterprise ");
            output.append("Application Platform (EAP) version 8.0; i.e. the version released in ");
            output.append("February 2024");
            output.append("{info}");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
        }

        if (containerId.startsWith("jonas"))
        {
            output.append("{note}Due to [license ");
            output.append("conflicts|https://codehaus-cargo.atlassian.net/browse/CARGO-1489], ");
            output.append("Codehaus Cargo versions 1.7.6 onwards do not contain the JOnAS ");
            output.append("Configurator in the Uberjar. If you plan to use the Codehaus Cargo ");
            output.append("JOnAS containers via [Java API|Javadocs] or [ANT tasks|Ant support], ");
            output.append("please [download the JOnAS Configurator JARs|");
            output.append("https://repo.maven.apache.org/maven2/org/ow2/jonas/tools/");
            output.append("configurator/] separately and put them in your classpath.");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
            output.append("This does not affect the [Maven 3 plugin], as it will download the ");
            output.append("required dependencies automatically (and, since Maven downloads and ");
            output.append("stores the JOnAS Configurator JARs separately from Codehaus Cargo ");
            output.append("you won't run into license issues).{note}");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
        }

        if (EnvironmentTestData.jakartaEeContainers.contains(containerId)
            || containerId.equals("payara"))
        {
            String containerName = null;
            for (Map.Entry<String, String> container : JAKARTAEE_CONTAINER_NAMES.entrySet())
            {
                if (containerId.startsWith(container.getKey()))
                {
                    containerName = container.getValue();
                    break;
                }
            }
            if (containerName == null)
            {
                throw new IllegalStateException(
                    "Jakarta EE container " + containerId + " not documented");
            }
            output.append("{note}");
            if (containerId.startsWith("jetty") && !containerId.equals("jetty11x"))
            {
                output.append("Jetty 12.x onwards support various versions of Jakarta EE, by ");
                output.append("adapting the {{[JettyPropertySet.MODULES|https://codehaus-cargo.");
                output.append("github.io/apidocs/org/codehaus/cargo/container/jetty/");
                output.append("JettyPropertySet.html#MODULES]}} and {{[JettyPropertySet.");
                output.append("DEPLOYER_EE_VERSION|https://codehaus-cargo.github.io/apidocs/org/");
                output.append("codehaus/cargo/container/jetty/JettyPropertySet.html#");
                output.append("DEPLOYER_EE_VERSION]}} configuration properties accordingly.");
                output.append(FileHandler.NEW_LINE);
                output.append(FileHandler.NEW_LINE);
                output.append("Users of the Jakarta EE 9 and above versions should be aware ");
                output.append("that, as a result of the move from Java EE to Jakarta EE as part ");
                output.append("of the transfer of Java EE to the Eclipse Foundation, the ");
                output.append("primary package for all implemented APIs has changed from ");
                output.append("{{javax.\\*}} to {{jakarta.\\*}}. This will almost certainly ");
                output.append("require code changes to enable applications to migrate to EE 9 ");
                output.append("and later.");
            }
            else
            {
                output.append("Users of " + containerName + ".x onwards should be aware that, ");
                output.append("as a result of the move from Java EE to Jakarta EE as part of ");
                output.append("the transfer of Java EE to the Eclipse Foundation, the primary ");
                output.append("package for all implemented APIs has changed from {{javax.\\*}} ");
                output.append("to {{jakarta.\\*}}. This will almost certainly require code ");
                output.append("changes to enable applications to migrate to ");
                output.append(containerName + ".x and later.");
            }
            if (containerId.startsWith("tomcat") || containerId.startsWith("tomee"))
            {
                output.append(FileHandler.NEW_LINE);
                output.append(FileHandler.NEW_LINE);
                output.append(containerName + ".x onwards has an integrated functionality for ");
                output.append("automatically migrating J2EE / Java EE WARs to Jakarta EE. ");
                output.append("You can follow the instructions on [Deploying legacy WARs to ");
                output.append("Tomcat 10.x onwards] to make use of it.");
            }
            output.append("{note}");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
        }

        if (containerId.equals("liberty"))
        {
            output.append("{info}The Codehaus Cargo WebSphere Liberty container is also ");
            output.append("compatible with Open Liberty.{info}");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
            output.append("{note}WebSphere Liberty 22.x onwards comes with multiple versions, ");
            output.append("some tagged as Java EE and others as Jakarta EE. Users of the ");
            output.append("Jakarta EE 9 versions should be aware that, as a result of the move ");
            output.append("from Java EE to Jakarta EE as part of the transfer of Java EE to the ");
            output.append("Eclipse Foundation, the primary package for all implemented APIs has ");
            output.append("changed from {{javax.\\*}} to {{jakarta.\\*}}. This will almost ");
            output.append("certainly require code changes to enable applications to migrate to ");
            output.append("EE 9 and later.");
            output.append("{note}");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
        }

        if (containerId.equals("wildfly22x") || containerId.equals("wildfly23x")
            || containerId.equals("wildfly24x") || containerId.equals("wildfly25x")
            || containerId.equals("wildfly26x"))
        {
            output.append("{note}WildFly ");
            output.append(containerId.replace("wildfly", "").replace("x", ""));
            output.append(" comes with two versions: Jakarta EE 8 Full & Web Distribution and ");
            output.append("the WildFly Preview EE 9 Distribution. Users of the WildFly Preview ");
            output.append("EE 9 Distribution should be aware that, as a result of the move ");
            output.append("from Java EE to Jakarta EE as part of the transfer of Java EE to the ");
            output.append("Eclipse Foundation, the primary package for all implemented APIs has ");
            output.append("changed from {{javax.\\*}} to {{jakarta.\\*}}. This will almost ");
            output.append("certainly require code changes to enable applications to migrate to ");
            output.append("EE 9 and later.");
            output.append("{note}");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
        }

        if (containerId.startsWith("weblogic12"))
        {
            if (containerId.equals("weblogic12x"))
            {
                output.append("{info}The WebLogic 12.x container lacks from the many features ");
                output.append("provided by the [WebLogic 12.1.x] and [WebLogic 12.2.x] ");
                output.append("containers - We would hence recommend using one of these ");
                output.append("instead of the WebLogic 12.x container.{info}");
                output.append(FileHandler.NEW_LINE);
                output.append(FileHandler.NEW_LINE);
            }

            output.append("{note}");
            output.append("If you are using the WebLogic 12 Developer's Edition, after you have ");
            output.append("downloaded the distribution, please remember to also configure ");
            output.append("WebLogic.");
            output.append(FileHandler.NEW_LINE);
            output.append("You can read http://www.oracle.com/webfolder/technetwork/tutorials/");
            output.append("obe/java/wls_12c_netbeans_install/wls_12c_netbeans_install.html for ");
            output.append("details.");
            output.append("{note}");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
        }

        if (containerId.startsWith("websphere"))
        {
            output.append("{note}");
            output.append("The WebSphere container does +not+ support WebSphere Application ");
            output.append("Server Community Edition, please prefer to use the \"full\" ");
            output.append("WebSphere package or WebSphere Application Server for Developers.");
            output.append("{note}");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
        }

        output.append(generateContainerFeaturesText(containerId));
        output.append(FileHandler.NEW_LINE);
        output.append(generateConfigurationFeaturesText(containerId));
        output.append(FileHandler.NEW_LINE);
        output.append(generateDeployerFeaturesText(containerId));
        output.append(FileHandler.NEW_LINE);
        output.append(generateOtherFeaturesText(containerId));
        output.append(FileHandler.NEW_LINE);
        output.append(generateConfigurationPropertiesText(containerId));
        output.append(FileHandler.NEW_LINE);
        output.append(generateSamplesInfoText(containerId));
        output.append(FileHandler.NEW_LINE);

        if (confluence != null)
        {
            String containerName = getContainerName(containerId);
            try
            {
                Page page = confluence.getPage("CARGO", containerName).get().get();
                confluence.storePage(page, Storage.of(output.toString(), Representation.WIKI));
            }
            catch (Exception e)
            {
                throw new Exception(
                    "Cannot update documentation for container: " + containerName, e);
            }
        }

        return output.toString();
    }

    /**
     * Generate documentation for the container features of a given container.
     * @param containerId Container id.
     * @return Generated container features documentation.
     */
    protected String generateContainerFeaturesText(String containerId)
    {
        StringBuilder output = new StringBuilder();

        output.append("h3.Container Features");
        output.append(FileHandler.NEW_LINE);
        output.append(FileHandler.NEW_LINE);
        output.append(
            "|| Feature name || Java API || Ant tasks || Maven 3 plugin || Comment ||");
        output.append(FileHandler.NEW_LINE);

        output.append("| [Container Instantiation] | ");
        output.append("(/) {{ContainerFactory.createContainer(\"" + containerId + "\"...)}} | ");
        output.append("(/) {{<cargo containerId=\"" + containerId + "\".../>}} |");
        output.append("(/) {{<containerId>" + containerId + "</containerId>}} | |");
        output.append(FileHandler.NEW_LINE);

        if (this.containerFactory.isContainerRegistered(containerId, ContainerType.INSTALLED)
            || this.containerFactory.isContainerRegistered(containerId, ContainerType.EMBEDDED))
        {
            output.append("| [Local Container] | (/) | (/) | (/) | |");
            output.append(FileHandler.NEW_LINE);
            if (containerId.equals("geronimo1x"))
            {
                output.append("| &nbsp; [Container Classpath] | (x) | (x) | (x) "
                    + "| Changing the the container classpath is not supported on "
                    + "Apache Geronimo 1.x |");
            }
            else if (containerId.startsWith("jboss7")
                || containerId.startsWith("wildfly") && !containerId.startsWith("wildfly-swarm"))
            {
                output.append("| &nbsp; [Container Classpath] | (/) | (/) | (/) "
                    + "| Read more on [JBoss 7.x onwards and WildFly container classpath] |");
            }
            else
            {
                output.append("| &nbsp; [Container Classpath] | (/) | (/) | (/) | |");
            }
            output.append(FileHandler.NEW_LINE);
            output.append("| &nbsp; [Container Start] | (/) | (/) | (/) | |");
            output.append(FileHandler.NEW_LINE);
            output.append("| &nbsp; [Container Stop] | (/) | (/) | (/) | |");
            output.append(FileHandler.NEW_LINE);
            output.append("| &nbsp; [Container Timeout] | (/) | (/) | (/) | |");
            output.append(FileHandler.NEW_LINE);

            if (this.containerFactory.isContainerRegistered(containerId, ContainerType.EMBEDDED))
            {
                output.append("| &nbsp; [Embedded Container] | ");
                output.append("(/) {{" + computedFQCN(this.containerFactory.getContainerClass(
                    containerId, ContainerType.EMBEDDED).getName()) + "}} | (/) | (/) | |");
            }
            else
            {
                output.append(
                    "| &nbsp; [Embedded Container] | (x) | (x) | (x) | |");
            }
            output.append(FileHandler.NEW_LINE);

            if (this.containerFactory.isContainerRegistered(containerId, ContainerType.INSTALLED))
            {
                output.append("| &nbsp; [Installed Container] | ");
                output.append("(/) {{" + computedFQCN(this.containerFactory.getContainerClass(
                    containerId, ContainerType.INSTALLED).getName()) + "}} | (/) | (/) | |");
                output.append(FileHandler.NEW_LINE);
                output.append(
                    "| &nbsp;&nbsp; [Passing system properties] | (/) | (/) | (/) | |");
                output.append(FileHandler.NEW_LINE);
                output.append(
                    "| &nbsp;&nbsp; [Installer] | (/) | (/) | (/) | |");
            }
            else
            {
                output.append(
                    "| &nbsp; [Installed Container] | (x) | (x) | (x) | |");
                output.append(FileHandler.NEW_LINE);
                output.append(
                    "| &nbsp;&nbsp; [Passing system properties] | (x) | (x) | (x) | |");
                output.append(FileHandler.NEW_LINE);
                output.append(
                    "| &nbsp;&nbsp; [Installer] | (x) | (x) | (x) | |");
            }
            output.append(FileHandler.NEW_LINE);
        }
        else
        {
            output.append("| [Local Container] | (x) | (x) | (x) | |");
            output.append(FileHandler.NEW_LINE);
            output.append("| &nbsp; [Container Classpath] | (x) | (x) | (x) | |");
            output.append(FileHandler.NEW_LINE);
            output.append("| &nbsp; [Container Start] | (x) | (x) | (x) | |");
            output.append(FileHandler.NEW_LINE);
            output.append("| &nbsp; [Container Stop] | (x) | (x) | (x) | |");
            output.append(FileHandler.NEW_LINE);
            output.append("| &nbsp; [Container Timeout] | (x) | (x) | (x) | |");
            output.append(FileHandler.NEW_LINE);
            output.append("| &nbsp; [Embedded Container] | (x) | (x) | (x) | |");
            output.append(FileHandler.NEW_LINE);
            output.append("| &nbsp; [Installed Container] | (x) | (x) | (x) | |");
            output.append(FileHandler.NEW_LINE);
            output.append("| &nbsp;&nbsp; [Passing system properties] | (x) | (x) | (x) | |");
            output.append(FileHandler.NEW_LINE);
            output.append("| &nbsp;&nbsp; [Installer] | (x) | (x) | (x) | |");
            output.append(FileHandler.NEW_LINE);
        }

        if (this.containerFactory.isContainerRegistered(containerId, ContainerType.REMOTE))
        {
            output.append("| [Remote Container] | ");
            output.append("(/) {{" + computedFQCN(this.containerFactory.getContainerClass(
                containerId, ContainerType.REMOTE).getName()) + "}} | (/) | (/) | |");
        }
        else
        {
            output.append("| [Remote Container] | (x) | (x) | (x) | |");
        }
        output.append(FileHandler.NEW_LINE);

        if (containerId.equals("tomcat7x") || containerId.equals("tomcat8x")
            || containerId.equals("tomcat9x") || containerId.equals("tomcat10x"))
        {
            output.append("{info:title=Running multiple Tomcat 7.x / Tomcat 8.x / Tomcat 9.x / ");
            output.append("Tomcat 10.x embedded containers within the same JVM}");
            output.append(FileHandler.NEW_LINE);
            output.append("Tomcat 7.x has introduced a class called ");
            output.append("{{TomcatURLStreamHandlerFactory}} where the singleton has a static ");
            output.append("{{instance}} field and a final {{registered}} attribute which are ");
            output.append("not always in sync and cause unexpected exceptions. Due to this, it ");
            output.append("is not possible to execute Tomcat 7.x, Tomcat 8.x, Tomcat 9.x and ");
            output.append("Tomcat 10.x embedded in the same JVM, in addition running one of ");
            output.append("these Tomcat embedded versions one after the other within the same ");
            output.append("JVM but different classpaths might also fail.");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
            output.append("If you want to be safe, prefer using the [Installed Container]. Also ");
            output.append("note that Tomcat 11.x onwards has gotten rid of that class.");
            output.append("{info}");
            output.append(FileHandler.NEW_LINE);
        }

        return output.toString();
    }

    /**
     * Generate documentation for the standalone configuration of a given container.
     * @param containerId Container id.
     * @param type Container type.
     * @return Generated standalone configuration documentation.
     */
    protected String generateStandaloneConfigurationText(String containerId,
        ContainerType type)
    {
        StringBuilder output = new StringBuilder();

        if (this.configurationFactory.isConfigurationRegistered(containerId, type,
            ConfigurationType.STANDALONE))
        {
            output.append("| [Standalone Local Configuration for " + type.getType()
                + " container|Standalone Local Configuration] | ");
            output.append("(/) {{" + computedFQCN(this.configurationFactory.getConfigurationClass(
                containerId, type, ConfigurationType.STANDALONE).getName())
                + "}} | (/) | (/) | |");
        }
        else
        {
            output.append("| [Standalone Local Configuration for " + type.getType()
                + " container|Standalone Local Configuration] | (x) | (x) | (x) | |");
        }
        return output.toString();
    }

    /**
     * Generate documentation for the existing configuration of a given container.
     * @param containerId Container id.
     * @param type Container type.
     * @return Generated existing configuration documentation.
     */
    protected String generateExistingConfigurationText(String containerId, ContainerType type)
    {
        StringBuilder output = new StringBuilder();

        if (this.configurationFactory.isConfigurationRegistered(containerId, type,
            ConfigurationType.EXISTING))
        {
            output.append("| [Existing Local Configuration for " + type.getType()
                + " container|Existing Local Configuration] | ");
            output.append("(/) {{" + computedFQCN(this.configurationFactory.getConfigurationClass(
                containerId, type, ConfigurationType.EXISTING).getName())
                + "}} | (/) | (/) | |");
        }
        else
        {
            output.append("| [Existing Local Configuration for " + type.getType()
                + " container|Existing Local Configuration] | (x) | (x) | (x) | |");
        }
        return output.toString();
    }

    /**
     * Generate documentation for the configuration features of a given container.
     * @param containerId Container id.
     * @return Generated configuration features documentation.
     */
    protected String generateConfigurationFeaturesText(String containerId)
    {
        StringBuilder output = new StringBuilder();

        output.append("h3.Configuration Features");
        output.append(FileHandler.NEW_LINE);
        output.append(FileHandler.NEW_LINE);
        output.append(
            "|| Feature name || Java API || Ant tasks || Maven 3 plugin || Comment ||");
        output.append(FileHandler.NEW_LINE);

        output.append(generateStandaloneConfigurationText(containerId, ContainerType.INSTALLED));
        output.append(FileHandler.NEW_LINE);
        output.append(generateStandaloneConfigurationText(containerId, ContainerType.EMBEDDED));
        output.append(FileHandler.NEW_LINE);

        output.append(generateExistingConfigurationText(containerId, ContainerType.INSTALLED));
        output.append(FileHandler.NEW_LINE);
        output.append(generateExistingConfigurationText(containerId, ContainerType.EMBEDDED));
        output.append(FileHandler.NEW_LINE);

        if (this.configurationFactory.isConfigurationRegistered(containerId, ContainerType.REMOTE,
            ConfigurationType.RUNTIME))
        {
            output.append("| [Runtime Configuration] | ");
            output.append("(/) {{" + computedFQCN(this.configurationFactory.getConfigurationClass(
                containerId, ContainerType.REMOTE, ConfigurationType.RUNTIME).getName())
                + "}} | (/) | (/) | |");
        }
        else
        {
            output.append("| [Runtime Configuration] | (x) | (x) | (x) | |");
        }
        output.append(FileHandler.NEW_LINE);

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.WAR))
        {
            output.append("| [Static deployment of WAR] | (/) | (/) | (/) | ");
            if (containerId.equals("tomcat4x"))
            {
                output.append("Does not support {{META-INF/context.xml}} files yet ");
            }
            output.append("|");
            output.append(FileHandler.NEW_LINE);

            // TODO: Need to introduce expanded WAR as a proper deployable type
            if (containerId.startsWith("geronimo"))
            {
                output.append("| [Static deployment of expanded WAR] | (x) | (x) | (x) | "
                    + "The Apache Geronimo container does not support expanded WARs |");
            }
            else if (containerId.startsWith("websphere"))
            {
                output.append("| [Static deployment of expanded WAR] | (x) | (x) | (x) | "
                    + "The WebSphere container does not support expanded WARs |");
            }
            else
            {
                output.append("| [Static deployment of expanded WAR] | (/) | (/) | (/) | |");
            }
        }
        else
        {
            output.append("| [Static deployment of WAR] | (x) | (x) | (x) | |");
        }
        output.append(FileHandler.NEW_LINE);

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.EJB))
        {
            output.append("| [Static deployment of EJB] | (/) | (/) | (/) | |");
        }
        else
        {
            output.append("| [Static deployment of EJB] | (x) | (x) | (x) | |");
        }
        output.append(FileHandler.NEW_LINE);

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.EAR))
        {
            output.append("| [Static deployment of EAR] | (/) | (/) | (/) | |");
        }
        else
        {
            output.append("| [Static deployment of EAR] | (x) | (x) | (x) | |");
        }
        output.append(FileHandler.NEW_LINE);

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.RAR))
        {
            output.append("| [Static deployment of RAR] | (/) | (/) | (/) | |");
        }
        else
        {
            output.append("| [Static deployment of RAR] | (x) | (x) | (x) | |");
        }
        output.append(FileHandler.NEW_LINE);

        if (containerId.startsWith("jboss") || containerId.startsWith("wildfly"))
        {
            if (this.containerCapabilityFactory.createContainerCapability(
                containerId).supportsDeployableType(DeployableType.HAR))
            {
                output.append("| [Static deployment of (JBoss) HAR] | (/) | (/) | (/) | |");
            }
            else
            {
                output.append("| [Static deployment of (JBoss) HAR] | (x) | (x) | (x) | |");
            }
            output.append(FileHandler.NEW_LINE);

            if (this.containerCapabilityFactory.createContainerCapability(
                containerId).supportsDeployableType(DeployableType.SAR))
            {
                output.append("| [Static deployment of (JBoss) SAR] | (/) | (/) | (/) | |");
            }
            else
            {
                output.append("| [Static deployment of (JBoss) SAR] | (x) | (x) | (x) | |");
            }
            output.append(FileHandler.NEW_LINE);

            if (this.containerCapabilityFactory.createContainerCapability(
                containerId).supportsDeployableType(DeployableType.AOP))
            {
                output.append("| [Static deployment of (JBoss) AOP] | (/) | (/) | (/) | |");
            }
            else
            {
                output.append("| [Static deployment of (JBoss) AOP] | (x) | (x) | (x) | |");
            }
            output.append(FileHandler.NEW_LINE);
        }

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.FILE))
        {
            output.append(
                "| [Static deployment of files] | (/) | (/) | (/) | |");
        }
        else
        {
            output.append(
                "| [Static deployment of files] | (x) | (x) | (x) | |");
        }
        output.append(FileHandler.NEW_LINE);

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.BUNDLE))
        {
            output.append(
                "| [Static deployment of OSGi Bundles] | (/) | (/) | (/) | |");
        }
        else
        {
            output.append(
                "| [Static deployment of OSGi Bundles] | (x) | (x) | (x) | |");
        }
        output.append(FileHandler.NEW_LINE);

        if (containerId.equals("jboss7x"))
        {
            output.append("{note}Even thought the JBoss 7.x container can deploy EJBs, JBoss ");
            output.append("7.0 itself it does not support remote EJB lookups yet.");
            output.append(FileHandler.NEW_LINE);
            output.append("If you plan to use remote EJBs on JBoss 7, please use ");
            output.append("[JBoss 7.1 or above|JBoss 7.1.x]. For further details, please read: ");
            output.append("http://community.jboss.org/message/616870{note}");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
        }

        return output.toString();
    }

    /**
     * Generate documentation for the deployer features of a given container.
     * @param containerId Container id.
     * @return Generated deployer features documentation.
     */
    protected String generateDeployerFeaturesText(String containerId)
    {
        StringBuilder output = new StringBuilder();

        output.append("h3.Deployer Features");
        output.append(FileHandler.NEW_LINE);
        output.append(FileHandler.NEW_LINE);

        if (containerId.equals("jonas5x"))
        {
            output.append("{note}The {{jonas5x}} local deployer requires the target JOnAS server "
                + "to be in {{development}} mode.");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
            output.append("If this is not the case, please use the {{jonas5x}} remote deployer.");
            output.append(FileHandler.NEW_LINE);
            output.append("Note that the {{jonas5x}} remote deployer can be used on a local "
                + "server by setting the {{GeneralPropertySet.HOSTNAME}} parameter to "
                + "{{localhost}}.{note}");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
        }

        output.append(
            "|| Feature name || Java API || Ant tasks || Maven 3 plugin || Comment ||");
        output.append(FileHandler.NEW_LINE);

        if (this.deployerFactory.isDeployerRegistered(containerId, DeployerType.INSTALLED))
        {
            output.append("| [Installed Deployer] | ");
            output.append("(/) {{" + computedFQCN(this.deployerFactory.getDeployerClass(
                containerId, DeployerType.INSTALLED).getName()) + "}} | (/) | (/) | |");
        }
        else
        {
            output.append("| [Installed Deployer] | (x) | (x) | (x) | |");
        }
        output.append(FileHandler.NEW_LINE);

        if (this.deployerFactory.isDeployerRegistered(containerId, DeployerType.EMBEDDED))
        {
            output.append("| [Embedded Deployer] | ");
            output.append("(/) {{" + computedFQCN(this.deployerFactory.getDeployerClass(
                containerId, DeployerType.EMBEDDED).getName()) + "}} | (/) | (/) | |");
        }
        else
        {
            output.append("| [Embedded Deployer] | (x) | (x) | (x) | |");
        }
        output.append(FileHandler.NEW_LINE);

        if (this.deployerFactory.isDeployerRegistered(containerId, DeployerType.REMOTE))
        {
            output.append("| [Remote Deployer] | ");
            output.append("(/) {{" + computedFQCN(this.deployerFactory.getDeployerClass(
                containerId, DeployerType.REMOTE).getName()) + "}} | (/) | (/) | |");
        }
        else
        {
            output.append("| [Remote Deployer] | (x) | (x) | (x) | |");
        }
        output.append(FileHandler.NEW_LINE);

        if (containerId.startsWith("glassfish") || containerId.startsWith("payara"))
        {
            String glassFishPropertySetLink = JAVADOC_URL_PREFIX
                    + GlassFishPropertySet.class.getName().replace('.', '/') + ".html#";
            output.append("{info:title=Adding arguments to the Deployer}");
            output.append(FileHandler.NEW_LINE);
            output.append("The ");
            if (containerId.startsWith("glassfish"))
            {
                output.append("GlassFish");
            }
            else
            {
                output.append("Payara");
            }
            output.append(" installed deployer allows for additional deployment and ");
            output.append("undeployment arguments by adding properties prefixed with ");
            output.append("{{[GlassFishPropertySet.DEPLOY_ARG_PREFIX|" + glassFishPropertySetLink);
            output.append("DEPLOY_ARG_PREFIX]}} and {{[GlassFishPropertySet.UNDEPLOY_ARG_PREFIX|");
            output.append(glassFishPropertySetLink + "UNDEPLOY_ARG_PREFIX]}} respectively ");
            output.append("followed by a number starting at {{1}}.");
            output.append("{info}");
            output.append(FileHandler.NEW_LINE);
        }

        if (containerId.equals("glassfish6x") || containerId.equals("glassfish7x")
            || containerId.equals("glassfish8x"))
        {
            output.append(FileHandler.NEW_LINE);
            output.append("{info}The GlassFish 6.x onwards containers do have any remote ");
            output.append("deployers, as GlassFish 6.0 onwards lack JSR-88 support.");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
            output.append("Please follow the [remote deployment instructions for GlassFish 6.x ");
            output.append("onwards|Remote deployments to GlassFish 6.x onwards and to recent ");
            output.append("Payara versions].");
            output.append("{info}");
        }

        return output.toString();
    }

    /**
     * Generate documentation for other features of a given container.
     * @param containerId Container id.
     * @return Generated other features' documentation.
     */
    protected String generateOtherFeaturesText(String containerId)
    {
        StringBuilder output = new StringBuilder();

        output.append("h3.Other Features");
        output.append(FileHandler.NEW_LINE);
        output.append(FileHandler.NEW_LINE);
        output.append(
            "|| Feature name || Java API || Ant tasks || Maven 3 plugin || Comment ||");
        output.append(FileHandler.NEW_LINE);

        output.append("| [Debugging] | (/) | (/) | (/) | |");
        output.append(FileHandler.NEW_LINE);

        return output.toString();
    }

    /**
     * Generate documentation for the configuration properties of a given container.
     * @param containerId Container id.
     * @return Generated configuration properties documentation.
     * @throws Exception If anything goes wrong.
     */
    protected String generateConfigurationPropertiesText(String containerId)
        throws Exception
    {
        StringBuilder output = new StringBuilder();

        output.append("h3.Supported Configuration properties");
        output.append(FileHandler.NEW_LINE);
        output.append(FileHandler.NEW_LINE);
        output.append("The tables below list both the [general configuration "
            + "properties|Configuration properties] as well as the container-specific ones.");
        output.append(FileHandler.NEW_LINE);
        output.append(FileHandler.NEW_LINE);

        if (this.configurationFactory.isConfigurationRegistered(containerId,
            ContainerType.INSTALLED, ConfigurationType.STANDALONE)
                || this.configurationFactory.isConfigurationRegistered(containerId,
                    ContainerType.EMBEDDED, ConfigurationType.STANDALONE))
        {
            output.append("h4.Standalone Local Configuration Properties");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);

            if (containerId.startsWith("websphere"))
            {
                output.append("{note}");
                output.append("Thought the WebSphere container supports [standalone local ");
                output.append("configurations|Standalone Local Configuration], it is ");
                output.append("recommended for you to prefer using WebSphere with an ");
                output.append("[existing local configuration|Existing Local Configuration]; ");
                output.append("i.e. a WebSphere profile you would create beforehand using ");
                output.append("WebSphere's setup commands and then use that with Codehaus Cargo.");
                output.append("{note}");
                output.append(FileHandler.NEW_LINE);
                output.append(FileHandler.NEW_LINE);
            }

            if (this.configurationFactory.isConfigurationRegistered(containerId,
                ContainerType.INSTALLED, ConfigurationType.STANDALONE))
            {
                output.append(generateConfigurationPropertiesForConfigurationTypeForContainerType(
                    "Standalone Local", ConfigurationType.STANDALONE, containerId,
                    ContainerType.INSTALLED));
                output.append(FileHandler.NEW_LINE);
            }
            if (this.configurationFactory.isConfigurationRegistered(containerId,
                ContainerType.EMBEDDED, ConfigurationType.STANDALONE))
            {
                output.append(generateConfigurationPropertiesForConfigurationTypeForContainerType(
                    "Standalone Local", ConfigurationType.STANDALONE, containerId,
                    ContainerType.EMBEDDED));
                output.append(FileHandler.NEW_LINE);
            }

            if (containerId.startsWith("jboss3") || containerId.startsWith("jboss4")
                || containerId.startsWith("jboss5") || containerId.startsWith("jboss6"))
            {
                output.append(FileHandler.NEW_LINE);
                output.append(FileHandler.NEW_LINE);
                output.append("{info}A side note on the JBoss ports:");
                output.append(FileHandler.NEW_LINE);
                output.append("* The {{cargo.rmi.port}} corresponds to the {{Port}} parameter of ");
                output.append("the {{jboss:service=Naming}} bean.");
                output.append(FileHandler.NEW_LINE);
                output.append("* The {{cargo.jboss.naming.port}} corresponds to the {{RmiPort}} ");
                output.append("parameter of the {{jboss:service=Naming}} bean.");
                output.append(FileHandler.NEW_LINE);
                output.append("{info}");
                output.append(FileHandler.NEW_LINE);
            }
            else if (containerId.startsWith("jonas"))
            {
                output.append(FileHandler.NEW_LINE);
                output.append("{info}In addition to the forementioned properties, you can set ");
                output.append("any JOnAS configuration property that's configurable via the ");
                output.append("JOnAS configurator using the {{");
                output.append(JonasPropertySet.CONFIGURATOR_PREFIX
                    .substring(0, JonasPropertySet.CONFIGURATOR_PREFIX.length() - 1));
                output.append("}} prefix. For example, to set the Tomcat AJP port, use the ");
                output.append("the property {{");
                output.append(JonasPropertySet.CONFIGURATOR_PREFIX);
                output.append("ajpPort}} and give the value you like.{info}");
                output.append(FileHandler.NEW_LINE);

                output.append("{note:Configuring JOnAS on JDK 9 and above}");
                output.append(FileHandler.NEW_LINE);
                output.append("As the JOnAS base ANT tasks, used by the configurator, make ");
                output.append("explicit use of the {{java.endorsed.dirs}} property which got ");
                output.append("removed with Java 9, it is no more possible to create any JOnAS ");
                output.append("configurations with newer versions of Java. You will get errors ");
                output.append("similar to the below:");
                output.append(FileHandler.NEW_LINE);
                output.append(FileHandler.NEW_LINE);
                output.append("{code}");
                output.append(FileHandler.NEW_LINE);
                output.append("[JmsRa] Cannot make a resource adaptor on RAConfig:");
                output.append(FileHandler.NEW_LINE);
                output.append("    [...]");
                output.append(FileHandler.NEW_LINE);
                output.append("Caused by: Java returned: 1");
                output.append(FileHandler.NEW_LINE);
                output.append("    at org.apache.tools.ant.taskdefs.Java.execute(Java.java:113)");
                output.append(FileHandler.NEW_LINE);
                output.append("    at org.ow2.jonas.antmodular.jonasbase.jms.JmsRa.execute");
                output.append("(JmsRa.java:222)");
                output.append(FileHandler.NEW_LINE);
                output.append("{code}");
                output.append(FileHandler.NEW_LINE);
                output.append("{note}");
                output.append(FileHandler.NEW_LINE);
            }
        }

        if (this.configurationFactory.isConfigurationRegistered(containerId,
            ContainerType.INSTALLED, ConfigurationType.EXISTING)
                || this.configurationFactory.isConfigurationRegistered(containerId,
                    ContainerType.EMBEDDED, ConfigurationType.EXISTING))
        {
            output.append("h4.Existing Local Configuration Properties");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
            if (this.configurationFactory.isConfigurationRegistered(containerId,
                ContainerType.INSTALLED, ConfigurationType.EXISTING))
            {
                output.append(generateConfigurationPropertiesForConfigurationTypeForContainerType(
                    "Existing Local", ConfigurationType.EXISTING, containerId,
                    ContainerType.INSTALLED));

                if (containerId.equals("jetty6x") || containerId.equals("jetty7x")
                    || containerId.equals("jetty8x") || containerId.equals("jetty9x"))
                {
                    output.append(FileHandler.NEW_LINE);
                    output.append("{info}If you specify {{cargo.runtime.args}} with ");
                    output.append("{{--ini=anyfile.ini}} (where {{anyfile.ini}} points to a ");
                    output.append("Jetty INI file), any property set in the Codehaus Cargo ");
                    output.append("Jetty container will be ignored and the ones read from the ");
                    output.append("INI file used instead.");
                    if (containerId.equals("jetty9x"))
                    {
                        output.append(FileHandler.NEW_LINE);
                        output.append(FileHandler.NEW_LINE);
                        output.append("Please note that the Jetty INI file concept doesn't ");
                        output.append("exist anymore with Jetty 9.1.x onwards.");
                    }
                    output.append("{info}");
                    output.append(FileHandler.NEW_LINE);
                }

                output.append(FileHandler.NEW_LINE);
            }
            if (this.configurationFactory.isConfigurationRegistered(containerId,
                ContainerType.EMBEDDED, ConfigurationType.EXISTING))
            {
                output.append(generateConfigurationPropertiesForConfigurationTypeForContainerType(
                    "Existing Local", ConfigurationType.EXISTING, containerId,
                    ContainerType.EMBEDDED));
                output.append(FileHandler.NEW_LINE);
            }
        }

        if (this.configurationFactory.isConfigurationRegistered(containerId,
            ContainerType.REMOTE, ConfigurationType.RUNTIME))
        {
            output.append("h4.Runtime Configuration Properties");
            if (containerId.startsWith("jboss"))
            {
                output.append(FileHandler.NEW_LINE);
                output.append(FileHandler.NEW_LINE);
                output.append("{info}Before using the JBoss remote deployer, ");
                output.append("please read: [JBoss Remote Deployer]{info}");
            }
            if (containerId.startsWith("jetty"))
            {
                output.append(FileHandler.NEW_LINE);
                output.append(FileHandler.NEW_LINE);
                output.append("{info}Before using the Jetty remote deployer, ");
                output.append("please read: [Jetty Remote Deployer]{info}");
            }
            else if (containerId.equals("glassfish3x") || containerId.equals("glassfish4x")
                || containerId.equals("glassfish5x") || containerId.equals("payara"))
            {
                output.append(FileHandler.NEW_LINE);
                output.append(FileHandler.NEW_LINE);
                output.append("{info}Before using the ");
                if (containerId.startsWith("glassfish"))
                {
                    output.append("GlassFish");
                }
                else
                {
                    output.append("Payara");
                }
                output.append(" remote deployer, please read: [JSR88]{info}");
            }
            if (containerId.startsWith("wildfly") && !containerId.startsWith("wildfly-swarm"))
            {
                output.append(FileHandler.NEW_LINE);
                output.append(FileHandler.NEW_LINE);
                output.append("{info}Before using the WildFly remote deployer, ");
                output.append("please read: [JBoss Remote Deployer]{info}");
            }
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
            output.append(generateConfigurationPropertiesForConfigurationTypeForContainerType(
                "Runtime", ConfigurationType.RUNTIME, containerId, ContainerType.REMOTE));
            output.append(FileHandler.NEW_LINE);
            if (containerId.equals("payara"))
            {
                output.append(FileHandler.NEW_LINE);
                output.append("{info}Recent versions of Payara might have [issues getting ");
                output.append("deployables uploaded via JSR-88|");
                output.append("https://codehaus-cargo.atlassian.net/browse/CARGO-1588], ");
                output.append("resulting in errors such as:");
                output.append(FileHandler.NEW_LINE);
                output.append("{code}");
                output.append("Distributing failed: Action failed Deploying application to ");
                output.append("target server failed; File not found");
                output.append("{code}");
                output.append(FileHandler.NEW_LINE);
                output.append(FileHandler.NEW_LINE);
                output.append("Please follow the [remote deployment instructions for recent ");
                output.append("Payara versions|Remote deployments to GlassFish 6.x onwards and ");
                output.append("to recent Payara versions].");
                output.append("{info}");
            }
            else if (containerId.equals("tomcat7x") || containerId.equals("tomcat8x")
                || containerId.equals("tomcat9x") || containerId.equals("tomcat10x")
                || containerId.equals("tomcat11x") || containerId.startsWith("tomee"))
            {
                output.append("{info}With ");
                if (containerId.startsWith("tomee"))
                {
                    output.append("TomEE");
                }
                else
                {
                    output.append("Tomcat ");
                    output.append(containerId.replace("tomcat", "").replace("x", ""));
                }
                output.append(", the Tomcat manager has multiple aspects to be careful about:");
                output.append(FileHandler.NEW_LINE);
                output.append("* Your browser by default accesses the HTML-based manager ");
                output.append("whereas Codehaus Cargo needs to use the text-based manager. As a ");
                output.append("result, if you want to set the {{RemotePropertySet.URI}} ");
                output.append("manually, please make sure you set the URL for the text-based ");
                output.append("manager, for example {{http://production27:8080/manager/text}}");
                output.append(FileHandler.NEW_LINE);
                output.append("* The text-based manager requires to be accessed by a user with ");
                output.append("the {{manager-script}} role; and by default no user has that ");
                output.append("role. As a result, please make sure you modify your ");
                output.append("{{tomcat-users.xml}} file to give that role to a user.");
                output.append(FileHandler.NEW_LINE);
                output.append("You can read more in the Tomcat documentation: ");
                if (containerId.equals("tomcat7x") || containerId.equals("tomee1x"))
                {
                    output.append("https://tomcat.apache.org/tomcat-7.0-doc/manager-howto.html");
                }
                else if (containerId.equals("tomcat8x") || containerId.equals("tomee7x"))
                {
                    output.append("https://tomcat.apache.org/tomcat-8.0-doc/manager-howto.html");
                }
                else if (containerId.equals("tomcat9x") || containerId.equals("tomee8x"))
                {
                    output.append("https://tomcat.apache.org/tomcat-9.0-doc/manager-howto.html");
                }
                else if (containerId.equals("tomcat10x") || containerId.equals("tomee9x")
                    || containerId.equals("tomee10x"))
                {
                    output.append("https://tomcat.apache.org/tomcat-10.0-doc/manager-howto.html");
                }
                else if (containerId.equals("tomcat11x"))
                {
                    output.append("https://tomcat.apache.org/tomcat-11.0-doc/manager-howto.html");
                }
                output.append("{info}");
                output.append(FileHandler.NEW_LINE);
            }
        }

        return output.toString();
    }

    /**
     * Generate documentation for the configuration properties of a given container.
     * @param typeAsName Configuration type's "human" name.
     * @param type Configuration type.
     * @param containerId Container id.
     * @param containerType Container type.
     * @return Generated configuration properties documentation.
     * @throws Exception If anything goes wrong.
     */
    protected String generateConfigurationPropertiesForConfigurationTypeForContainerType(
        String typeAsName, ConfigurationType type, String containerId, ContainerType containerType)
        throws Exception
    {
        StringBuilder output = new StringBuilder();

        output.append("h5. For " + containerType + " container " + computedFQCN(
            this.containerFactory.getContainerClass(containerId, containerType).getName()));
        output.append(FileHandler.NEW_LINE);
        output.append(FileHandler.NEW_LINE);

        output.append(
            "|| Property name || Java Property || Supported? || Default value || Javadoc ||");
        output.append(FileHandler.NEW_LINE);

        Class configurationClass = Class.forName(
            this.configurationFactory.getConfigurationClass(containerId, containerType,
                type).getName());

        Configuration slc;
        if (type != ConfigurationType.RUNTIME)
        {
            slc = (LocalConfiguration)
                configurationClass.getConstructor(String.class).newInstance("whatever");
        }
        else
        {
            slc = (RuntimeConfiguration) configurationClass.getDeclaredConstructor().newInstance();
        }

        boolean supportsDatasourceOrResource = false;
        Map<String, Boolean> properties = this.configurationCapabilityFactory.
            createConfigurationCapability(containerId, containerType, type).getProperties();
        if (containerId.startsWith("tomcat") && containerType.equals(ContainerType.EMBEDDED))
        {
            properties = new HashMap<>(properties);
            properties.put(TomcatPropertySet.EMBEDDED_OVERRIDE_JAVA_LOGGING, Boolean.TRUE);
        }
        Set<String> sortedPropertyNames = new TreeSet<String>(properties.keySet());
        for (String property : sortedPropertyNames)
        {
            if (property.equals(GeneralPropertySet.SPAWN_PROCESS)
                && ContainerType.EMBEDDED.equals(containerType))
            {
                // Embedded containers don't support SPAWN_PROCESS
                continue;
            }

            if (property.startsWith(DATASOURCE_PREFIX)
                || ResourcePropertySet.RESOURCE.equals(property))
            {
                // These are documented afterwards
                supportsDatasourceOrResource = true;
                continue;
            }

            Field propertySetField = findPropertySetFieldName(property);
            output.append("| [" + property + "|Configuration properties] | ");
            if (propertySetField != null)
            {
                String propertySetFieldClassName =
                    propertySetField.getDeclaringClass().getSimpleName();
                output.append("[" + propertySetFieldClassName + "."
                    + propertySetField.getName() + "|Configuration properties]");
            }
            output.append(" | ");
            boolean supported = properties.get(property);
            output.append(supported ? "(/)" : "(x)");
            switch (property)
            {
                case GeneralPropertySet.JAVA_HOME:
                    String javaVersion;
                    String extra = "";
                    if ("jetty7x".equals(containerId))
                    {
                        javaVersion = "5 if no datasources are to be deployed, 6 otherwise";
                    }
                    else if ("jetty9x".equals(containerId))
                    {
                        javaVersion =
                            "7 (Jetty 9.0.x, 9.1.x and 9.2.x) or 8 (Jetty 9.3.x and 9.4.x)";
                    }
                    else if ("jonas5x".equals(containerId))
                    {
                        javaVersion = "5 (JOnAS 5.0.x, 5.1.x and 5.2.x) or 6 (JOnAS 5.3.x)";
                        extra = "Due to a bug parsing the Java version in the OW2 utilities, "
                            + "JOnAS 5.x doesn't run on Java 8 and above";
                    }
                    else if ("glassfish6x".equals(containerId))
                    {
                        javaVersion = "8 (GlassFish 6.0.x) or 11 (GlassFish 6.1.x and above)";
                        extra = "GlassFish 6.0.x [only runs on Java 8|"
                            + "https://github.com/eclipse-ee4j/glassfish/issues/23102]{_}"
                            + FileHandler.NEW_LINE
                            + "{_}Only GlassFish 6.2.1 onwards supports Java 17";
                    }
                    else if ("payara".equals(containerId))
                    {
                        javaVersion = ":{_}" + FileHandler.NEW_LINE
                            + "* {_}7 (Payara 4.x){_}" + FileHandler.NEW_LINE
                            + "* {_}8 (Payara 5.x){_}" + FileHandler.NEW_LINE
                            + "* {_}11 (Payara  6.x){_}" + FileHandler.NEW_LINE
                            + "* {_}21 (Payara 7.x onwards){_}" + FileHandler.NEW_LINE + "{_}";
                        extra = "Payara 4.x [doesn't run on Java 9 and above|"
                            + "https://github.com/eclipse-ee4j/glassfish/issues/22130]";
                    }
                    else if ("resin3x".equals(containerId) || "resin31x".equals(containerId))
                    {
                        javaVersion = "6, as the Codehaus Cargo_ {{[ResinRun|"
                            + "https://codehaus-cargo.github.io/apidocs/org/codehaus/cargo/"
                            + "container/resin/internal/ResinRun.html]}} _class requires Java 6";
                    }
                    else if ("liberty".equals(containerId))
                    {
                        javaVersion = "7 (Java EE 7 version) or 8 (Java EE 8 version)";
                    }
                    else if ("tomcat10x".equals(containerId))
                    {
                        javaVersion = "8 (Tomcat 10.0.x) or 11 (Tomcat 10.1.x onwards)";
                    }
                    else if ("tomcat11x".equals(containerId))
                    {
                        javaVersion = "17 (up to and including Tomcat 11.0.0 M6) or "
                            + "21 (Tomcat 11.0.0 M7 onwards)";
                    }
                    else if ("tomee7x".equals(containerId))
                    {
                        javaVersion = "7 (TomEE 7.0.x) or 8 (TomEE 7.1.x)";
                    }
                    else if ("tomee9x".equals(containerId))
                    {
                        javaVersion = "8 (up to and including TomEE 9.0.0 M7) or "
                            + "11 (TomEE 9.0.0 M8 onwards)";
                    }
                    else if ("tomee10x".equals(containerId))
                    {
                        javaVersion = "11 (TomEE 10.0.0 M1) or "
                            + "17 (TomEE 10.0.0 M2 onwards)";
                    }
                    else if ("weblogic12x".equals(containerId)
                        || "weblogic121x".equals(containerId))
                    {
                        javaVersion =
                            "6 for earlier versions, WebLogic 12.1.3 onwards require Java 7";
                    }
                    else
                    {
                        String pomJavaVersion = getContainerServerJavaVersion(containerId);
                        javaVersion =
                            pomJavaVersion.replace("${cargo.java.home.1_", "").replace("}", "");
                    }

                    if (JAVA7_MAX_CONTAINERS_JSP.contains(containerId))
                    {
                        extra = "Due to incompatibilities with its JSP environment, "
                            + getContainerName(containerId) + " doesn't run on Java 8 and above";
                    }
                    else if (JAVA7_MAX_CONTAINERS_OSGI.contains(containerId))
                    {
                        extra = "Due to incompatibilities with its OSGi environment, "
                            + getContainerName(containerId) + " doesn't run on Java 8 and above";
                    }
                    else if ("resin3x".equals(containerId))
                    {
                        extra = "Due to incompatibilities between_ "
                            + "{{com.caucho.log.EnvironmentLogger}} _and the behaviour described "
                            + "in [JDK-8015098|https://bugs.openjdk.java.net/browse/JDK-8015098], "
                            + "Resin 3.x doesn't run on Java 7 and above";
                    }
                    else if ("resin4x".equals(containerId))
                    {
                        extra = "As opposed to what the Resin documentation indicates, all "
                            + "Resin 4.x versions require Java 8 or above";
                    }
                    else if (containerId.startsWith("websphere"))
                    {
                        extra = "By default, Codehaus Cargo will use the JVM from the "
                            + "WebSphere installation directory";
                    }
                    if (!extra.isEmpty())
                    {
                        extra = FileHandler.NEW_LINE + "&nbsp;" + FileHandler.NEW_LINE
                            + "{_}" + extra + "{_}";
                    }
                    if (javaVersion.charAt(0) != ':')
                    {
                        javaVersion = " " + javaVersion + " ";
                    }
                    output.append(
                        " | {_}JAVA_HOME version" + javaVersion + "or newer{_}" + extra + " |");
                    break;
                case JonasPropertySet.JONAS_SERVICES_LIST:
                    output.append(" | {_}Will be loaded from the{_} {{conf/jonas.properties}} "
                        + "{_}file in the container home directory{_} |");
                    break;
                case TomcatPropertySet.EMBEDDED_OVERRIDE_JAVA_LOGGING:
                    output.append(" | {{false}} |");
                    break;
                default:
                    output.append(" | " + (slc.getPropertyValue(property) == null ? "N/A"
                        : "{{" + slc.getPropertyValue(property) + "}}") + " |");
                    break;
            }
            if (supported && propertySetField != null)
            {
                String propertySetFieldUrl = JAVADOC_URL_PREFIX
                    + propertySetField.getDeclaringClass().getName().replace('.', '/') + ".html#"
                    + propertySetField.getName();
                output.append(" [(*g)|" + propertySetFieldUrl + "]");
            }
            output.append(" |");
            output.append(FileHandler.NEW_LINE);
        }

        if (supportsDatasourceOrResource)
        {
            output.append("{info:title=Datasource and Resource configuration}");
            output.append(FileHandler.NEW_LINE);
            output.append("In addition to the forementioned properties, this container ");
            output.append("configuration can also set up datasources and/or resources. ");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
            output.append("For more details, please read: [DataSource and Resource Support].");
            output.append(FileHandler.NEW_LINE);
            output.append("{info}");
            output.append(FileHandler.NEW_LINE);
            if ("jetty12x".equals(containerId))
            {
                output.append("{note}");
                output.append(FileHandler.NEW_LINE);
                output.append("Since the Jetty 12.x {{jetty-plus}} modules are specific to EE ");
                output.append("versions, datasources and resources are deployed using each ");
                output.append("application's {{context.xml}} file, matching the EE version.");
                output.append(FileHandler.NEW_LINE);
                output.append(FileHandler.NEW_LINE);
                output.append("The below are hence pre-requisites to using datasources or ");
                output.append("resources in Jetty 12.x:");
                output.append(FileHandler.NEW_LINE);
                output.append("* {{[JettyPropertySet.DEPLOYER_CREATE_CONTEXT_XML|https://");
                output.append("codehaus-cargo.github.io/apidocs/org/codehaus/cargo/container/");
                output.append("jetty/JettyPropertySet.html#DEPLOYER_CREATE_CONTEXT_XML]}} must ");
                output.append("must be set to {{true}}.");
                output.append(FileHandler.NEW_LINE);
                output.append("* {{[JettyPropertySet.MODULES|https://codehaus-cargo.github.io/");
                output.append("apidocs/org/codehaus/cargo/container/jetty/JettyPropertySet.html#");
                output.append("MODULES]}} must contain the right {{jetty-plus}} modules for the ");
                output.append("application(s) in scope.");
                output.append(FileHandler.NEW_LINE);
                output.append("* {{[JettyPropertySet.DEPLOYER_EE_VERSION|https://codehaus-cargo.");
                output.append("github.io/apidocs/org/codehaus/cargo/container/jetty/");
                output.append("JettyPropertySet.html#DEPLOYER_EE_VERSION]}} must be set aligned ");
                output.append("with the {{jetty-plus}} module EE version mentioned above.");
                output.append(FileHandler.NEW_LINE);
                output.append("{note}");
            }
        }

        if (ConfigurationType.STANDALONE.equals(type)
            && !ContainerType.EMBEDDED.equals(containerType) && ("jetty9x".equals(containerId)
            || containerId.startsWith("jetty1")))
        {
            output.append("{info:title=How to run Jetty under a Java Security Manager}");
            output.append(FileHandler.NEW_LINE);
            output.append(
                "Jetty 9.x and above can be configured to run under a Java Security Manager.");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
            output.append("For further information on how to achieve this, please refer to the ");
            output.append("[associated tip in the Maven 3 Plugin Tips page|");
            output.append("Maven 3 Plugin Tips#tip4].");
            output.append(FileHandler.NEW_LINE);
            output.append("{info}");
            output.append(FileHandler.NEW_LINE);
        }

        if (ConfigurationType.STANDALONE.equals(type) && ("tomcat8x".equals(containerId)
            || "tomcat9x".equals(containerId) || "tomcat10x".equals(containerId)
            || "tomcat11x".equals(containerId)))
        {
            output.append("{info:title=Configuring HTTP/2 for Tomcat 8.5 and above}");
            output.append(FileHandler.NEW_LINE);
            output.append("Tomcat 8.5 and above support HTTP/2.");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
            output.append("For further information on how to enable this, please refer to ");
            output.append("[Configuring HTTP/2 for Tomcat 8.5 and above].");
            output.append(FileHandler.NEW_LINE);
            output.append("{info}");
            output.append(FileHandler.NEW_LINE);
        }

        if (ConfigurationType.STANDALONE.equals(type) && containerId.startsWith("weblogic"))
        {
            output.append("{note}");
            output.append(FileHandler.NEW_LINE);
            output.append("Some versions of WebLogic require you to have a complex password, ");
            output.append("i.e. only {{weblogic}} is not enough.");
            output.append(FileHandler.NEW_LINE);
            output.append("If you get an error message similar to the below, please include a ");
            output.append("number in your WebLogic password, for example {{weblogic1}}.");
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
            output.append("{code}");
            output.append(FileHandler.NEW_LINE);
            output.append("<Critical> <WebLogicServer> <BEA-000386> <Server subsystem failed. ");
            output.append("Reason: java.lang.AssertionError:");
            output.append("java.lang.reflect.InvocationTargetException");
            output.append(FileHandler.NEW_LINE);
            output.append("    at weblogic.descriptor.DescriptorManager$");
            output.append("SecurityServiceImpl$SecurityProxy._invokeServiceMethod");
            output.append(FileHandler.NEW_LINE);
            output.append("    at weblogic.descriptor.DescriptorManager$SecurityServiceImpl$");
            output.append("SecurityProxy.decrypt");
            output.append(FileHandler.NEW_LINE);
            output.append("    at weblogic.descriptor.DescriptorManager$");
            output.append("SecurityServiceImpl.decrypt");
            output.append(FileHandler.NEW_LINE);
            output.append("    at weblogic.descriptor.internal.AbstractDescriptorBean._decrypt");
            output.append(FileHandler.NEW_LINE);
            output.append("    at weblogic.management.configuration.");
            output.append("SecurityConfigurationMBeanImpl.getCredential");
            output.append(FileHandler.NEW_LINE);
            output.append("    at weblogic.security.internal.ServerPrincipalValidatorImpl.");
            output.append("getSecret");
            output.append(FileHandler.NEW_LINE);
            output.append("    at weblogic.security.internal.ServerPrincipalValidatorImpl.sign");
            output.append("{code}");
            output.append(FileHandler.NEW_LINE);
            output.append("{note}");
            output.append(FileHandler.NEW_LINE);
        }

        return output.toString();
    }

    /**
     * Create the short class name for a given class name.
     * @param className Original class name.
     * @return Short class name.
     */
    protected String computedFQCN(String className)
    {
        return "o.c.c.c" + className.substring(
            className.substring(0, className.lastIndexOf('.')).lastIndexOf('.'));
    }

    /**
     * Name of a given container.
     * @param containerId Container ID.
     * @return Name of the container.
     */
    protected String getContainerName(String containerId)
    {
        ContainerType containerType;
        if (this.containerFactory.isContainerRegistered(containerId, ContainerType.INSTALLED))
        {
            containerType = ContainerType.INSTALLED;
        }
        else
        {
            containerType = ContainerType.EMBEDDED;
        }
        Configuration configuration;
        if (this.configurationFactory.isConfigurationRegistered(
            containerId, containerType, ConfigurationType.STANDALONE))
        {
            configuration = this.configurationFactory.createConfiguration(
                containerId, containerType, ConfigurationType.STANDALONE);
        }
        else
        {
            configuration = this.configurationFactory.createConfiguration(
                containerId, containerType, ConfigurationType.EXISTING);
        }
        return this.containerFactory.createContainer(
            containerId, containerType, configuration).getName()
                .replace(" Embedded", "")
                    .replaceAll("WildFly Swarm.*", "WildFly Swarm")
                        .replaceAll(" \\((JBoss )?EAP .*\\)", "");
    }

    /**
     * Find the property set field for a given value.
     * @param propertyValue Property value name.
     * @return Property set field for the given value.
     * @throws Exception If anything goes wrong.
     */
    protected Field findPropertySetFieldName(String propertyValue) throws Exception
    {
        for (Class propertySetClasse : PROPERTY_SET_CLASSES)
        {
            Field result = findPropertySetField(propertyValue, propertySetClasse);
            if (result != null)
            {
                return result;
            }
        }

        return null;
    }

    /**
     * Find the property set field name for a given value on a given class.
     * @param propertyValue Property value name.
     * @param propertySetClass Class name.
     * @return Property set field for the given value, <code>null</code> if the given class does
     * not have such a value.
     * @throws Exception If anything goes wrong.
     */
    protected Field findPropertySetField(String propertyValue, Class propertySetClass)
        throws Exception
    {
        Field[] fields = propertySetClass.getFields();
        for (Field field : fields)
        {
            String value = (String) field.get(null);
            if (value.equals(propertyValue))
            {
                return field;
            }
        }

        return null;
    }

    /**
     * Generate documentation for the Samples of a given container.
     * @param containerId Container id.
     * @return Generated configuration properties documentation.
     * @throws Exception If anything goes wrong.
     */
    protected String generateSamplesInfoText(String containerId)
        throws Exception
    {
        String url = getContainerServerDownloadUrl(containerId);

        if (url != null)
        {
            StringBuilder output = new StringBuilder();

            output.append("h3.Tested On");
            output.append(FileHandler.NEW_LINE);

            if ("geronimo2x".equals(containerId))
            {
                output.append("Due to incompatibilities between the way Geronimo 2.x handles ");
                output.append("JAVA_HOME and the multi-version setup in our Continous ");
                output.append("Integration system (which has Java 6 as the lowest JDK version ");
                output.append("for testing samples and Java 8 for compiling and packaging ");
                output.append("Codehaus Cargo), the Geronimo 2.x container is not tested ");
                output.append("automatically.");
            }
            else
            {
                output.append("This container is automatically tested by the "
                    + "[Continous Integration system|https://codehaus-cargo.semaphoreci.com/"
                    + "projects/cargo] every time there is a code change.");
                output.append(FileHandler.NEW_LINE);
                if (containerId.startsWith("wildfly-swarm"))
                {
                    output.append("The WildFly Swarm version used during tests is: {{");
                    output.append(url);
                    output.append("}}");
                }
                else
                {
                    output.append("The server used for tests is downloaded from: ");
                    output.append(url);
                }
            }
            output.append(FileHandler.NEW_LINE);
            output.append(FileHandler.NEW_LINE);
            return output.toString();
        }
        else
        {
            return "";
        }
    }

    /**
     * Returns the Java version used for testing the given container.
     * @param containerId Container ID.
     * @return Java version for testing <code>containerId</code>.
     */
    public String getContainerServerJavaVersion(String containerId)
    {
        File pom = new File(SAMPLES_DIRECTORY, POM).getAbsoluteFile();

        Model model = new Model();
        try
        {
            model = POM_READER.read(new FileReader(pom));
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Caught Exception reading " + pom, e);
        }

        MavenProject project = new MavenProject(model);
        project.setFile(pom);

        Map<String, Plugin> plugins = project.getPluginManagement().getPluginsAsMap();
        Plugin surefire = plugins.get(SUREFIRE_PLUGIN);
        if (surefire == null)
        {
            throw new IllegalStateException("Cannot find plugin " + SUREFIRE_PLUGIN
                + " in pom file " + pom + ". Found plugins: " + plugins.keySet());
        }

        Xpp3Dom configuration = (Xpp3Dom) surefire.getConfiguration();
        if (configuration == null)
        {
            throw new IllegalStateException("Plugin " + SUREFIRE_PLUGIN + " in pom file " + pom
                + " does not have any configuration.");
        }
        Xpp3Dom systemPropertyVariables = configuration.getChild(SYSTEM_PROPERTY_VARIABLES);
        if (systemPropertyVariables == null)
        {
            throw new IllegalStateException(
                "Plugin " + SUREFIRE_PLUGIN + " in pom file " + pom + " does not have any "
                    + SYSTEM_PROPERTY_VARIABLES + " in its configuration.");
        }
        Xpp3Dom javaHome = systemPropertyVariables.getChild("cargo." + containerId + ".java.home");
        if (javaHome == null)
        {
            throw new IllegalArgumentException("No java.home for " + containerId);
        }
        return javaHome.getValue();
    }

    /**
     * Returns the download URL used for testing the given container.
     * @param containerId Container ID.
     * @return Download URL for testing <code>containerId</code>, <code>null</code> if no download
     * URL is set.
     */
    public String getContainerServerDownloadUrl(String containerId)
    {
        File pom;
        if (containerId.startsWith("wildfly-swarm"))
        {
            pom = new File(CARGO_ROOT_DIRECTORY, POM).getAbsoluteFile();
        }
        else
        {
            pom = new File(SAMPLES_DIRECTORY, POM).getAbsoluteFile();
        }

        Model model = new Model();
        try
        {
            model = POM_READER.read(new FileReader(pom));
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Caught Exception reading " + pom, e);
        }

        MavenProject project = new MavenProject(model);
        project.setFile(pom);

        if (containerId.startsWith("wildfly-swarm"))
        {
            return project.getProperties().getProperty("wildfly-swarm.version");
        }
        else
        {
            Map<String, Plugin> plugins = project.getPluginManagement().getPluginsAsMap();
            Plugin surefire = plugins.get(SUREFIRE_PLUGIN);
            if (surefire == null)
            {
                throw new IllegalStateException("Cannot find plugin " + SUREFIRE_PLUGIN
                    + " in pom file " + pom + ". Found plugins: " + plugins.keySet());
            }

            Xpp3Dom configuration = (Xpp3Dom) surefire.getConfiguration();
            if (configuration == null)
            {
                throw new IllegalStateException("Plugin " + SUREFIRE_PLUGIN + " in pom file " + pom
                    + " does not have any configuration.");
            }
            Xpp3Dom systemPropertyVariables = configuration.getChild(SYSTEM_PROPERTY_VARIABLES);
            if (systemPropertyVariables == null)
            {
                throw new IllegalStateException(
                    "Plugin " + SUREFIRE_PLUGIN + " in pom file " + pom + " does not have any "
                        + SYSTEM_PROPERTY_VARIABLES + " in its configuration.");
            }
            Xpp3Dom url = systemPropertyVariables.getChild("cargo." + containerId + ".url");
            if (url == null)
            {
                return null;
            }
            return url.getValue();
        }
    }
}
