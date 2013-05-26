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
package org.codehaus.cargo.documentation;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.ContainerType;
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
import org.codehaus.cargo.container.weblogic.WebLogicPropertySet;
import org.codehaus.cargo.container.websphere.WebSpherePropertySet;
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
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Generate container documentation using Confluence markup language. The generated text is meant to
 * be copied on the Cargo Confluence web site.
 * 
 * @version $Id$
 */
public class ConfluenceContainerDocumentationGenerator
{
    /**
     * Line separator character.
     */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Containers that work on Java 4.
     */
    private static final List<String> JAVA4_CONTAINERS = Arrays.asList(new String[] {
        "geronimo1x",
        "jboss3x",
        "jboss4x",
        "jetty4x",
        "jetty5x",
        "jo1x",
        "jonas4x",
        "oc4j9x",
        "resin2x",
        "tomcat4x",
        "tomcat5x",
        "weblogic8x"
    });

    /**
     * Containers that work on Java 5.
     */
    private static final List<String> JAVA5_CONTAINERS = Arrays.asList(new String[] {
        "geronimo2x",
        "glassfish2x",
        "jboss42x",
        "jboss5x",
        "jboss51x",
        "jetty6x",
        "jetty7x",
        "jonas5x",
        "jrun4x",
        "oc4j10x",
        "resin3x",
        "resin31x",
        "tomcat6x",
        "weblogic9x",
        "weblogic10x"
    });

    /**
     * Containers that work on Java 6.
     */
    private static final List<String> JAVA6_CONTAINERS = Arrays.asList(new String[] {
        "geronimo3x",
        "glassfish3x",
        "jboss6x",
        "jboss61x",
        "jboss7x",
        "jboss71x",
        "jetty8x",
        "resin4x",
        "tomcat7x",
        "weblogic103x",
        "weblogic12x",
        "websphere85x",
        "wildfly8x"
    });

    /**
     * Classes that are used to get the property names.
     */
    private static final Class[] PROPERTY_SET_CLASSES = {
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
        WebLogicPropertySet.class,
        WebSpherePropertySet.class
    };

    /**
     * Prefix for all datasource-related properties.
     */
    private static final String DATASOURCE_PREFIX = "cargo.datasource.";

    /**
     * Prefix for all Javadoc URLs.
     */
    private static final String JAVADOC_URL_PREFIX =
        "http://cargo.codehaus.org/maven-site/cargo-core/apidocs/";

    /**
     * The MavenXpp3Reader used to get project info from pom files.
     */
    private static final MavenXpp3Reader POM_READER = new MavenXpp3Reader();

    /**
     * Relative path to the cargo-core-samples directory.
     */
    private static final String SAMPLES_DIRECTORY = System.getProperty("basedir") + "/../samples/";

    /**
     * Constant for known POM file name.
     */
    private static final String POM = "pom.xml";

    /**
     * Constant for the Surefire plugin name.
     */
    private static final String SUREFIRE_PLUGIN = "org.apache.maven.plugins:maven-surefire-plugin";

    /**
     * Constant for the systemProperties node of the Surefire plugin.
     */
    private static final String SYSTEM_PROPERTIES = "systemProperties";

    /**
     * Constant for the CI URL.
     */
    private static final String CI_URL = "https://bamboo-ci.codehaus.org/browse/CARGO-SAMPLES";

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
        output.append(LINE_SEPARATOR);
        output.append(LINE_SEPARATOR);

        output.append("|| Container || Configuration || Resource || DataSource || "
            + "Transactional DataSource || XA DataSource ||");
        output.append(LINE_SEPARATOR);

        Map<String, Set<ContainerType>> containerIds = containerFactory.getContainerIds();
        SortedMap<String, String> sortedContainerIds = new TreeMap<String, String>();
        for (String containerId : containerIds.keySet())
        {
            String sortedContainerId = containerId.
                replace("10", "9y").
                replace("12", "9z").
                replace("x", "0x");
            sortedContainerIds.put(sortedContainerId, containerId);
        }
        for (String containerId : sortedContainerIds.values())
        {
            Map<String, Boolean> properties;
            try
            {
                properties = this.configurationCapabilityFactory.
                createConfigurationCapability(
                    containerId, ContainerType.INSTALLED, ConfigurationType.STANDALONE
                ).getProperties();
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
                    Container container = this.containerFactory.createContainer(
                        containerId, ContainerType.INSTALLED, configuration);

                    Class configurationClass = configuration.getClass();

                    output.append("| [");
                    output.append(container.getName());
                    output.append("] | {{");
                    output.append(computedFQCN(configurationClass.getName()));
                    output.append("}} | (");
                    if (properties.keySet().contains(ResourcePropertySet.RESOURCE))
                    {
                        output.append('/');
                    }
                    else
                    {
                        output.append('x');
                    }
                    output.append(") | (");
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
                    output.append(LINE_SEPARATOR);

                    break;
                }
            }
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
        output.append(LINE_SEPARATOR);
        output.append(LINE_SEPARATOR);

        if (containerId.equals("geronimo1x"))
        {
            output.append("{note}The Geronimo 1.x CARGO container REQUIRES Geronimo 1.1.1.{note}");
            output.append(LINE_SEPARATOR);
            output.append(LINE_SEPARATOR);
        }
        else if (containerId.startsWith("websphere"))
        {
            output.append("{note}");
            output.append("The WebSphere container does +not+ support WebSphere Application ");
            output.append("Server Community Edition,");
            output.append(LINE_SEPARATOR);
            output.append("please prefer to use the \"full\" WebSphere package ");
            output.append("or WebSphere Application Server for Developers.");
            output.append(LINE_SEPARATOR);
            output.append(LINE_SEPARATOR);
            output.append("{note}");
            output.append(LINE_SEPARATOR);
            output.append(LINE_SEPARATOR);
        }

        output.append(generateContainerFeaturesText(containerId));
        output.append(LINE_SEPARATOR);
        output.append(generateConfigurationFeaturesText(containerId));
        output.append(LINE_SEPARATOR);
        output.append(generateDeployerFeaturesText(containerId));
        output.append(LINE_SEPARATOR);
        output.append(generateOtherFeaturesText(containerId));
        output.append(LINE_SEPARATOR);
        output.append(generateConfigurationPropertiesText(containerId));
        output.append(LINE_SEPARATOR);
        output.append(generateSamplesInfoText(containerId));
        output.append(LINE_SEPARATOR);

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
        output.append(LINE_SEPARATOR);
        output.append(LINE_SEPARATOR);
        output.append("|| Feature name || Java || Ant || Maven2 || Comment ||");
        output.append(LINE_SEPARATOR);

        output.append("| [Container Instantiation]               | ");
        output.append("(/) {{ContainerFactory.createContainer(\"" + containerId + "\"...)}} | ");
        output.append("(/) {{<cargo containerId=\"" + containerId + "\".../>}} |");
        output.append("(/) {{<containerId>" + containerId + "</containerId>}} | |");
        output.append(LINE_SEPARATOR);

        if (this.containerFactory.isContainerRegistered(containerId, ContainerType.INSTALLED)
            || this.containerFactory.isContainerRegistered(containerId, ContainerType.EMBEDDED))
        {
            output.append("| [Local Container]                       | (/) | (/) | (/) | |");
            output.append(LINE_SEPARATOR);
            if (containerId.equals("geronimo1x"))
            {
                output.append("| &nbsp; [Container Classpath]            | (x) | (x) | (x) "
                    + "| Changing the the container classpath is not supported on "
                    + "Apache Geronimo 1.x |");
            }
            else if (containerId.equals("jboss7x"))
            {
                output.append("| &nbsp; [Container Classpath]            | (/) | (/) | (/) "
                    + "| The JBoss 7.x deployer will modify the {{MANIFEST.MF}} of your "
                        + "deployables in order to add the extra and shared classpath |");
            }
            else if (containerId.equals("jboss71x"))
            {
                output.append("| &nbsp; [Container Classpath]            | (/) | (/) | (/) "
                    + "| The JBoss 7.1.x deployer will modify the {{MANIFEST.MF}} of your "
                        + "deployables in order to add the extra and shared classpath |");
            }
            else if (containerId.equals("wildfly8x"))
            {
                output.append("| &nbsp; [Container Classpath]            | (/) | (/) | (/) "
                    + "| The WildFly 8.x deployer will modify the {{MANIFEST.MF}} of your "
                        + "deployables in order to add the extra and shared classpath |");
            }
            else
            {
                output.append("| &nbsp; [Container Classpath]            | (/) | (/) | (/) | |");
            }
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp; [Container Start]                | (/) | (/) | (/) | |");
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp; [Container Stop]                 | (/) | (/) | (/) | |");
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp; [Container Timeout]              | (/) | (/) | (/) | |");
            output.append(LINE_SEPARATOR);

            if (this.containerFactory.isContainerRegistered(containerId, ContainerType.EMBEDDED))
            {
                output.append("| &nbsp; [Embedded Container]             | ");
                output.append("(/) {{" + computedFQCN(this.containerFactory.getContainerClass(
                    containerId, ContainerType.EMBEDDED).getName()) + "}} | (/) | (/) | |");
            }
            else
            {
                output.append(
                    "| &nbsp; [Embedded Container]             | (x) | (x) | (x) | |");
            }
            output.append(LINE_SEPARATOR);

            if (this.containerFactory.isContainerRegistered(containerId, ContainerType.INSTALLED))
            {
                output.append("| &nbsp; [Installed Container]            | ");
                output.append("(/) {{" + computedFQCN(this.containerFactory.getContainerClass(
                    containerId, ContainerType.INSTALLED).getName()) + "}} | (/) | (/) | |");
                output.append(LINE_SEPARATOR);
                output.append(
                    "| &nbsp;&nbsp; [Passing system properties]| (/) | (/) | (/) | |");
                output.append(LINE_SEPARATOR);
                output.append(
                    "| &nbsp;&nbsp; [Installer]                | (/) | (/) | (/) | |");
            }
            else
            {
                output.append(
                    "| &nbsp; [Installed Container]            | (x) | (x) | (x) | |");
                output.append(LINE_SEPARATOR);
                output.append(
                    "| &nbsp;&nbsp; [Passing system properties]| (x) | (x) | (x) | |");
                output.append(LINE_SEPARATOR);
                output.append(
                    "| &nbsp;&nbsp; [Installer]                | (x) | (x) | (x) | |");
            }
            output.append(LINE_SEPARATOR);
        }
        else
        {
            output.append("| [Local Container]                       | (x) | (x) | (x) | |");
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp; [Container Classpath]            | (x) | (x) | (x) | |");
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp; [Container Start]                | (x) | (x) | (x) | |");
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp; [Container Stop]                 | (x) | (x) | (x) | |");
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp; [Container Timeout]              | (x) | (x) | (x) | |");
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp; [Embedded Container]             | (x) | (x) | (x) | |");
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp; [Installed Container]            | (x) | (x) | (x) | |");
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp;&nbsp; [Passing system properties]| (x) | (x) | (x) | |");
            output.append(LINE_SEPARATOR);
            output.append("| &nbsp;&nbsp; [Installer]                | (x) | (x) | (x) | |");
            output.append(LINE_SEPARATOR);
        }

        if (this.containerFactory.isContainerRegistered(containerId, ContainerType.REMOTE))
        {
            output.append("| [Remote Container]                      | ");
            output.append("(/) {{" + computedFQCN(this.containerFactory.getContainerClass(
                containerId, ContainerType.REMOTE).getName()) + "}} | (/) | (/) | |");
        }
        else
        {
            output.append("| [Remote Container]                      | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

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
                + " container|Standalone Local Configuration]        | ");
            output.append("(/) {{" + computedFQCN(this.configurationFactory.getConfigurationClass(
                containerId, type, ConfigurationType.STANDALONE).getName())
                + "}} | (/) | (/) | |");
        }
        else
        {
            output.append("| [Standalone Local Configuration for " + type.getType()
                + " container|Standalone Local Configuration]        | (x) | (x) | (x) | |");
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
                + " container|Existing Local Configuration]          | ");
            output.append("(/) {{" + computedFQCN(this.configurationFactory.getConfigurationClass(
                containerId, type, ConfigurationType.EXISTING).getName())
                + "}} | (/) | (/) | |");
        }
        else
        {
            output.append("| [Existing Local Configuration for " + type.getType()
                + " container|Existing Local Configuration]          | (x) | (x) | (x) | |");
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
        output.append(LINE_SEPARATOR);
        output.append(LINE_SEPARATOR);
        output.append("|| Feature name || Java || Ant || Maven2 || Comment ||");
        output.append(LINE_SEPARATOR);

        output.append(generateStandaloneConfigurationText(containerId, ContainerType.INSTALLED));
        output.append(LINE_SEPARATOR);
        output.append(generateStandaloneConfigurationText(containerId, ContainerType.EMBEDDED));
        output.append(LINE_SEPARATOR);

        output.append(generateExistingConfigurationText(containerId, ContainerType.INSTALLED));
        output.append(LINE_SEPARATOR);
        output.append(generateExistingConfigurationText(containerId, ContainerType.EMBEDDED));
        output.append(LINE_SEPARATOR);

        if (this.configurationFactory.isConfigurationRegistered(containerId, ContainerType.REMOTE,
            ConfigurationType.RUNTIME))
        {
            output.append("| [Runtime Configuration]                 | ");
            output.append("(/) {{" + computedFQCN(this.configurationFactory.getConfigurationClass(
                containerId, ContainerType.REMOTE, ConfigurationType.RUNTIME).getName())
                + "}} | (/) | (/) | |");
        }
        else
        {
            output.append("| [Runtime Configuration]                 | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.WAR))
        {
            output.append("| [Static deployment of WAR]              | (/) | (/) | (/) | ");
            if (containerId.equals("tomcat4x"))
            {
                output.append("Does not support {{META-INF/context.xml}} files yet ");
            }
            output.append("|");
            output.append(LINE_SEPARATOR);

            // TODO: Need to introduce expanded WAR as a proper deployable type
            if (containerId.startsWith("geronimo"))
            {
                output.append("| [Static deployment of expanded WAR]     | (x) | (x) | (x) | "
                    + "The Apache Geronimo container does not support expanded WARs |");
            }
            else if (containerId.startsWith("websphere"))
            {
                output.append("| [Static deployment of expanded WAR]     | (x) | (x) | (x) | "
                    + "The WebSphere container does not support expanded WARs |");
            }
            else
            {
                output.append("| [Static deployment of expanded WAR]     | (/) | (/) | (/) | |");
            }
        }
        else
        {
            output.append("| [Static deployment of WAR]              | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.EJB))
        {
            output.append("| [Static deployment of EJB]              | (/) | (/) | (/) | |");
        }
        else
        {
            output.append("| [Static deployment of EJB]              | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.EAR))
        {
            output.append("| [Static deployment of EAR]              | (/) | (/) | (/) | |");
        }
        else
        {
            output.append("| [Static deployment of EAR]              | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.RAR))
        {
            output.append("| [Static deployment of RAR]              | (/) | (/) | (/) | |");
        }
        else
        {
            output.append("| [Static deployment of RAR]              | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        if (containerId.startsWith("jboss") || containerId.startsWith("wildfly"))
        {
            if (this.containerCapabilityFactory.createContainerCapability(
                containerId).supportsDeployableType(DeployableType.HAR))
            {
                output.append("| [Static deployment of (JBoss) HAR]      | (/) | (/) | (/) | |");
            }
            else
            {
                output.append("| [Static deployment of (JBoss) HAR]      | (x) | (x) | (x) | |");
            }
            output.append(LINE_SEPARATOR);
    
            if (this.containerCapabilityFactory.createContainerCapability(
                containerId).supportsDeployableType(DeployableType.SAR))
            {
                output.append("| [Static deployment of (JBoss) SAR]      | (/) | (/) | (/) | |");
            }
            else
            {
                output.append("| [Static deployment of (JBoss) SAR]      | (x) | (x) | (x) | |");
            }
            output.append(LINE_SEPARATOR);
    
            if (this.containerCapabilityFactory.createContainerCapability(
                containerId).supportsDeployableType(DeployableType.AOP))
            {
                output.append("| [Static deployment of (JBoss) AOP]      | (/) | (/) | (/) | |");
            }
            else
            {
                output.append("| [Static deployment of (JBoss) AOP]      | (x) | (x) | (x) | |");
            }
            output.append(LINE_SEPARATOR);
        }

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.FILE))
        {
            output.append(
                "| [Static deployment of files]              | (/) | (/) | (/) | |");
        }
        else
        {
            output.append(
                "| [Static deployment of files]              | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        if (this.containerCapabilityFactory.createContainerCapability(
            containerId).supportsDeployableType(DeployableType.BUNDLE))
        {
            output.append(
                "| [Static deployment of OSGi Bundles]              | (/) | (/) | (/) | |");
        }
        else
        {
            output.append(
                "| [Static deployment of OSGi Bundles]              | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        if (containerId.equals("jboss7x"))
        {
            output.append("{note}Even thought the JBoss 7.x container can deploy EJBs, JBoss ");
            output.append("7.0 itself it does not support remote EJB lookups yet.");
            output.append(LINE_SEPARATOR);
            output.append("If you plan to use remote EJBs on JBoss 7, please use ");
            output.append("[JBoss 7.1 or above|JBoss 7.1.x]. For further details, please read: ");
            output.append("http://community.jboss.org/message/616870{note}");
            output.append(LINE_SEPARATOR);
            output.append(LINE_SEPARATOR);
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
        output.append(LINE_SEPARATOR);
        output.append(LINE_SEPARATOR);

        if (containerId.equals("jonas5x"))
        {
            output.append("{note}The {{jonas5x}} local deployer requires the target JOnAS server "
                + "to be in {{development}} mode.");
            output.append(LINE_SEPARATOR);
            output.append(LINE_SEPARATOR);
            output.append("If this is not the case, please use the {{jonas5x}} remote deployer.");
            output.append(LINE_SEPARATOR);
            output.append("Note that the {{jonas5x}} remote deployer can be used on a local "
                + "server by setting the {{GeneralPropertySet.HOSTNAME}} parameter to "
                + "{{localhost}}.{note}");
            output.append(LINE_SEPARATOR);
            output.append(LINE_SEPARATOR);
        }

        output.append("|| Feature name || Java || Ant || Maven2 || Comment ||");
        output.append(LINE_SEPARATOR);

        if (this.deployerFactory.isDeployerRegistered(containerId, DeployerType.INSTALLED))
        {
            output.append("| [Installed Deployer]                    | ");
            output.append("(/) {{" + computedFQCN(this.deployerFactory.getDeployerClass(
                containerId, DeployerType.INSTALLED).getName()) + "}} | (/) | (/) | |");
        }
        else
        {
            output.append("| [Installed Deployer]                    | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        if (this.deployerFactory.isDeployerRegistered(containerId, DeployerType.EMBEDDED))
        {
            output.append("| [Embedded Deployer]                     | ");
            output.append("(/) {{" + computedFQCN(this.deployerFactory.getDeployerClass(
                containerId, DeployerType.EMBEDDED).getName()) + "}} | (/) | (/) | |");
        }
        else
        {
            output.append("| [Embedded Deployer]                     | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

        if (this.deployerFactory.isDeployerRegistered(containerId, DeployerType.REMOTE))
        {
            output.append("| [Remote Deployer]                       | ");
            output.append("(/) {{" + computedFQCN(this.deployerFactory.getDeployerClass(
                containerId, DeployerType.REMOTE).getName()) + "}} | (/) | (/) | |");
        }
        else
        {
            output.append("| [Remote Deployer]                       | (x) | (x) | (x) | |");
        }
        output.append(LINE_SEPARATOR);

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
        output.append(LINE_SEPARATOR);
        output.append(LINE_SEPARATOR);
        output.append("|| Feature name || Java || Ant || Maven2 || Comment ||");
        output.append(LINE_SEPARATOR);

        output.append("| [Debugging]                             | (/) | (/) | (/) | |");
        output.append(LINE_SEPARATOR);

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
        output.append(LINE_SEPARATOR);
        output.append(LINE_SEPARATOR);
        output.append("The tables below list both the [general configuration "
            + "properties|Configuration properties] as well as the container-specific ones.");
        output.append(LINE_SEPARATOR);
        output.append(LINE_SEPARATOR);

        if (this.configurationFactory.isConfigurationRegistered(containerId,
            ContainerType.INSTALLED, ConfigurationType.STANDALONE)
                || this.configurationFactory.isConfigurationRegistered(containerId,
                    ContainerType.EMBEDDED, ConfigurationType.STANDALONE))
        {
            output.append("h4.Standalone Local Configuration Properties");
            output.append(LINE_SEPARATOR);
            output.append(LINE_SEPARATOR);
            if (this.configurationFactory.isConfigurationRegistered(containerId,
                ContainerType.INSTALLED, ConfigurationType.STANDALONE))
            {
                output.append(generateConfigurationPropertiesForConfigurationTypeForContainerType(
                    "Standalone Local", ConfigurationType.STANDALONE, containerId,
                    ContainerType.INSTALLED));
                output.append(LINE_SEPARATOR);
            }
            if (this.configurationFactory.isConfigurationRegistered(containerId,
                ContainerType.EMBEDDED, ConfigurationType.STANDALONE))
            {
                output.append(generateConfigurationPropertiesForConfigurationTypeForContainerType(
                    "Standalone Local", ConfigurationType.STANDALONE, containerId,
                    ContainerType.EMBEDDED));
                output.append(LINE_SEPARATOR);
            }

            if (containerId.startsWith("jboss3") || containerId.startsWith("jboss4")
                || containerId.startsWith("jboss5") || containerId.startsWith("jboss6"))
            {
                output.append(LINE_SEPARATOR);
                output.append(LINE_SEPARATOR);
                output.append("{info}A side note on the JBoss ports:");
                output.append(LINE_SEPARATOR);
                output.append("* The {{cargo.rmi.port}} corresponds to the {{Port}} parameter of");
                output.append(" the {{jboss:service=Naming}} bean.");
                output.append(LINE_SEPARATOR);
                output.append("* The {{cargo.jboss.naming.port}} corresponds to the {{RmiPort}}");
                output.append(" parameter of the {{jboss:service=Naming}} bean.");
                output.append(LINE_SEPARATOR);
                output.append("{info}");
                output.append(LINE_SEPARATOR);
            }
            else if (containerId.startsWith("jonas"))
            {
                output.append(LINE_SEPARATOR);
                output.append("{info}In addition to the forementioned properties, you can set ");
                output.append("any JOnAS configuration property that's configurable via the ");
                output.append("JOnAS configurator using the {{");
                output.append(JonasPropertySet.CONFIGURATOR_PREFIX
                    .substring(0, JonasPropertySet.CONFIGURATOR_PREFIX.length() - 1));
                output.append("}} prefix. For example, to set the Tomcat AJP port, use the ");
                output.append("the property {{");
                output.append(JonasPropertySet.CONFIGURATOR_PREFIX);
                output.append("ajpPort}} and give the value you like.{info}");
                output.append(LINE_SEPARATOR);
            }
        }

        if (this.configurationFactory.isConfigurationRegistered(containerId,
            ContainerType.INSTALLED, ConfigurationType.EXISTING)
                || this.configurationFactory.isConfigurationRegistered(containerId,
                    ContainerType.EMBEDDED, ConfigurationType.EXISTING))
        {
            output.append("h4.Existing Local Configuration Properties");
            output.append(LINE_SEPARATOR);
            output.append(LINE_SEPARATOR);
            if (this.configurationFactory.isConfigurationRegistered(containerId,
                ContainerType.INSTALLED, ConfigurationType.EXISTING))
            {
                output.append(generateConfigurationPropertiesForConfigurationTypeForContainerType(
                    "Existing Local", ConfigurationType.EXISTING, containerId,
                    ContainerType.INSTALLED));

                if (containerId.startsWith("jetty"))
                {
                    output.append(LINE_SEPARATOR);
                    output.append("{info}If you specify {{cargo.runtime.args}} with ");
                    output.append("{{--ini=anyfile.ini}} (where {{anyfile.ini}} points to a ");
                    output.append("Jetty INI file), any property set in the CARGO Jetty ");
                    output.append("container will be ignored and the ones read from the INI file ");
                    output.append("used instead.{info}");
                    output.append(LINE_SEPARATOR);
                }

                output.append(LINE_SEPARATOR);
            }
            if (this.configurationFactory.isConfigurationRegistered(containerId,
                ContainerType.EMBEDDED, ConfigurationType.EXISTING))
            {
                output.append(generateConfigurationPropertiesForConfigurationTypeForContainerType(
                    "Existing Local", ConfigurationType.EXISTING, containerId,
                    ContainerType.EMBEDDED));
                output.append(LINE_SEPARATOR);
            }
        }

        if (this.configurationFactory.isConfigurationRegistered(containerId,
            ContainerType.REMOTE, ConfigurationType.RUNTIME))
        {
            output.append("h4.Runtime Configuration Properties");
            if (containerId.startsWith("jboss"))
            {
                output.append(LINE_SEPARATOR);
                output.append(LINE_SEPARATOR);
                output.append("{info}Before using the JBoss remote deployer, ");
                output.append("please read: [JBoss Remote Deployer]{info}");
            }
            if (containerId.startsWith("jetty"))
            {
                output.append(LINE_SEPARATOR);
                output.append(LINE_SEPARATOR);
                output.append("{info}Before using the Jetty remote deployer, ");
                output.append("please read: [Jetty Remote Deployer]{info}");
            }
            else if (containerId.equals("glassfish3x"))
            {
                output.append(LINE_SEPARATOR);
                output.append(LINE_SEPARATOR);
                output.append("{info}Before using the GlassFish remote deployer, ");
                output.append("please read: [JSR88]{info}");
            }
            if (containerId.startsWith("wildfly"))
            {
                output.append(LINE_SEPARATOR);
                output.append(LINE_SEPARATOR);
                output.append("{info}Before using the WildFly remote deployer, ");
                output.append("please read: [JBoss Remote Deployer]{info}");
            }
            output.append(LINE_SEPARATOR);
            output.append(LINE_SEPARATOR);
            output.append(generateConfigurationPropertiesForConfigurationTypeForContainerType(
                "Runtime", ConfigurationType.RUNTIME, containerId, ContainerType.REMOTE));
            output.append(LINE_SEPARATOR);
            if (containerId.equals("tomcat7x"))
            {
                output.append("{info}With Tomcat 7, the Tomcat manager has multiple aspects to ");
                output.append("be careful about:");
                output.append(LINE_SEPARATOR);
                output.append("* Your browser by default accesses the HTML-based manager ");
                output.append("whereas CARGO needs to use the text-based manager. As a result, ");
                output.append("if you want to set the {{RemotePropertySet.URI}} manually, ");
                output.append("please make sure you set the URL for the text-based manager, ");
                output.append("for example {{http://production27:8080/manager/text}}");
                output.append(LINE_SEPARATOR);
                output.append("* The text-based manager requires to be accessed by a user with ");
                output.append("the {{manager-script}} role; and by default no user has that ");
                output.append("role. As a result, please make sure you modify your ");
                output.append("{{tomcat-users.xml}} file to give that role to a user.");
                output.append(LINE_SEPARATOR);
                output.append("You can read more on the Tomcat documentation: ");
                output.append("http://tomcat.apache.org/tomcat-7.0-doc/manager-howto.html{info}");
                output.append(LINE_SEPARATOR);
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
        output.append(LINE_SEPARATOR);
        output.append(LINE_SEPARATOR);

        output.append(
            "|| Property name || Java Property || Supported? || Default value || Javadoc ||");
        output.append(LINE_SEPARATOR);

        Class configurationClass = Class.forName(
            this.configurationFactory.getConfigurationClass(containerId, containerType,
                type).getName());

        Configuration slc;
        if (type != ConfigurationType.RUNTIME)
        {
            slc = (LocalConfiguration) configurationClass.getConstructor(
                new Class[] {String.class}).newInstance(new Object[] {"whatever"});
        }
        else
        {
            slc = (RuntimeConfiguration) configurationClass.newInstance();
        }

        boolean supportsDatasourceOrResource = false;
        Map<String, Boolean> properties = this.configurationCapabilityFactory.
            createConfigurationCapability(containerId, containerType, type).getProperties();
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
            if (GeneralPropertySet.JAVA_HOME.equals(property))
            {
                String javaVersion;
                String extra = "";

                if (JAVA4_CONTAINERS.contains(containerId))
                {
                    javaVersion = "4";
                }
                else if (JAVA5_CONTAINERS.contains(containerId))
                {
                    javaVersion = "5";
                }
                else if (JAVA6_CONTAINERS.contains(containerId))
                {
                    javaVersion = "6";
                }
                else
                {
                    javaVersion = "7";
                }

                if (containerId.startsWith("websphere"))
                {
                    extra = LINE_SEPARATOR + "{_}By default, CARGO will use the JVM from the "
                        + "WebSphere installation directory{_}";
                }

                output.append(
                    " | {_}JAVA_HOME version " + javaVersion + " or newer{_}" + extra + " |");
            }
            else
            {
                output.append(" | " + (slc.getPropertyValue(property) == null ? "N/A"
                    : "{{" + slc.getPropertyValue(property) + "}}") + " |");
            }
            if (supported && propertySetField != null)
            {
                String propertySetFieldUrl = JAVADOC_URL_PREFIX
                    + propertySetField.getDeclaringClass().getName().replace('.', '/') + ".html#"
                    + propertySetField.getName();
                output.append(" [(*g)|" + propertySetFieldUrl + "]");
            }
            output.append(" |");
            output.append(LINE_SEPARATOR);
        }

        if (supportsDatasourceOrResource)
        {
            output.append("{info:title=Datasource and Resource configuration}");
            output.append(LINE_SEPARATOR);
            output.append("In addition to the forementioned properties, this container ");
            output.append("configuration can also set up datasources and/or resources. ");
            output.append(LINE_SEPARATOR);
            output.append(LINE_SEPARATOR);
            output.append("For more details, please read: [DataSource and Resource Support].");
            output.append(LINE_SEPARATOR);
            output.append("{info}");
            output.append(LINE_SEPARATOR);
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
            className.substring(0, className.lastIndexOf(".")).lastIndexOf("."));
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
            output.append(LINE_SEPARATOR);

            output.append("This container is automatically tested on its server using the "
                + "Codehaus Cargo Continous Integration System once a day.");
            output.append(LINE_SEPARATOR);
            output.append("* The server used for tests is downloaded from: ");
            output.append(url);
            output.append(LINE_SEPARATOR);
            output.append("* Link to the build plan: ");
            output.append(CI_URL + containerId.toUpperCase(Locale.ENGLISH));
            output.append(LINE_SEPARATOR);
            output.append(LINE_SEPARATOR);
            return output.toString();
        }
        else
        {
            return "";
        }
    }

    /**
     * Returns the download URL used for testing the given container.
     * @param containerId Container ID.
     * @return Download URL for testing <code>containerId</code>, <code>null</null> if no download
     * URL is set.
     */
    public String getContainerServerDownloadUrl(String containerId)
    {
        File pom = new File(SAMPLES_DIRECTORY, POM).getAbsoluteFile();

        Model model = new Model();
        try
        {
            model = POM_READER.read(new FileReader(pom));
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Caught Exception reading pom.xml", e);
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
        Xpp3Dom systemProperties = configuration.getChild(SYSTEM_PROPERTIES);
        if (systemProperties == null)
        {
            throw new IllegalStateException("Plugin " + SUREFIRE_PLUGIN + " in pom file " + pom
                + " does not have any " + SYSTEM_PROPERTIES + " in its configuration.");
        }

        String urlName = "cargo." + containerId + ".url";
        for (Xpp3Dom property : systemProperties.getChildren())
        {
            Xpp3Dom nameChild = property.getChild("name");
            Xpp3Dom valueChild = property.getChild("value");
            if (nameChild == null || valueChild == null)
            {
                throw new IllegalStateException("One of the " + SUREFIRE_PLUGIN
                    + "'s configuration options in pom file " + pom + " is incomplete:\n"
                        + property);
            }

            if (urlName.equals(nameChild.getValue()))
            {
                return valueChild.getValue();
            }
        }

        return null;
    }
}
