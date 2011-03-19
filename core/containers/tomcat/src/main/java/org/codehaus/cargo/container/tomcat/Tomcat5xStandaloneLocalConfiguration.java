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
package org.codehaus.cargo.container.tomcat;

import java.util.Properties;
import java.util.Set;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.internal.util.PropertyUtils;
import org.codehaus.cargo.container.tomcat.internal.AbstractCatalinaStandaloneLocalConfiguration;
import org.codehaus.cargo.container.tomcat.internal.Tomcat5And6xConfigurationBuilder;

/**
 * StandAloneLocalConfiguration that is appropriate for Tomcat 5.x containers.
 * <p>
 * This code needs to work with both {@link Tomcat5xInstalledLocalContainer} and
 * {@link Tomcat5xEmbeddedLocalContainer}.
 * 
 * @version $Id$
 */
public class Tomcat5xStandaloneLocalConfiguration extends
    AbstractCatalinaStandaloneLocalConfiguration
{

    /**
     * used to insert DataSources and Resources into the configuration file.
     */
    private Tomcat5And6xConfigurationBuilder configurationBuilder;

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCatalinaStandaloneLocalConfiguration#AbstractCatalinaStandaloneLocalConfiguration(String)
     */
    public Tomcat5xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(TomcatPropertySet.CONNECTOR_EMPTY_SESSION_PATH, "true");
        configurationBuilder = new Tomcat5And6xConfigurationBuilder();

    }

    /**
     * {@inheritDoc}
     * 
     * @see Tomcat5And6xConfigurationBuilder
     */
    @Override
    protected ConfigurationBuilder createConfigurationBuilder(LocalContainer container)
    {
        return configurationBuilder;
    }

    /**
     * {@inheritDoc} this does not deploy the manager, if the application is embedded.
     * 
     * @see AbstractCatalinaStandaloneLocalConfiguration#setupManager(org.codehaus.cargo.container.LocalContainer)
     */
    @Override
    protected void setupManager(LocalContainer container)
    {
        if (container instanceof EmbeddedLocalContainer)
        {
            // when running in the embedded mode, there's no need
            // of any manager application.
        }
        else
        {
            String from = ((InstalledLocalContainer) container).getHome();
            String to = getHome();
            getFileHandler().copyDirectory(from + "/server/webapps/manager",
                to + "/server/webapps/manager");
            getFileHandler().copyFile(from + "/server/lib/catalina.jar",
                to + "/server/lib/catalina.jar");
            getFileHandler().copyFile(from + "/conf/Catalina/localhost/manager.xml",
                to + "/conf/Catalina/localhost/manager.xml");
        }
    }

    /**
     * Configure the emptySessionPath property token on the filter chain for the server.xml
     * configuration file. {@inheritDoc}
     * 
     * @see AbstractCatalinaStandaloneLocalConfiguration#createTomcatFilterChain()
     */
    @Override
    protected FilterChain createTomcatFilterChain()
    {
        FilterChain filterChain = super.createTomcatFilterChain();

        getAntUtils().addTokenToFilterChain(filterChain, "catalina.connector.emptySessionPath",
            getPropertyValue(TomcatPropertySet.CONNECTOR_EMPTY_SESSION_PATH));

        return filterChain;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "Tomcat 5.x Standalone Configuration";
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCatalinaStandaloneLocalConfiguration#setupManager(org.codehaus.cargo.container.LocalContainer)
     */
    @Override
    protected Set<String> getConfFiles()
    {
        Set<String> files = super.getConfFiles();
        files.add("catalina.properties");
        files.add("context.xml");
        return files;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupTransactionManager()
    {
        Resource transactionManagerResource =
            new Resource("UserTransaction", "javax.transaction.UserTransaction");

        Properties parameters = new Properties();
        PropertyUtils.setPropertyIfNotNull(parameters, "jotm.timeout", "60");
        PropertyUtils.setPropertyIfNotNull(parameters, "factory",
            "org.objectweb.jotm.UserTransactionFactory");
        transactionManagerResource.setParameters(PropertyUtils.toMap(parameters));
        getResources().add(transactionManagerResource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getXpathForResourcesParent()
    {
        return "//Context";
    }

    /**
     * {@inheritDoc} In Tomcat 5.5+, we use context.xml to avoid configuration problems.
     */
    @Override
    protected String getOrCreateResourceConfigurationFile(Resource rs, LocalContainer container)
    {
        String confDir = getFileHandler().createDirectory(getHome(), "conf");
        return getFileHandler().append(confDir, "context.xml");
    }

}
