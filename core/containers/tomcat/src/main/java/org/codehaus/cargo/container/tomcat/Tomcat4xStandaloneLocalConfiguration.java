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

import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.internal.util.PropertyUtils;
import org.codehaus.cargo.container.tomcat.internal.AbstractCatalinaStandaloneLocalConfiguration;
import org.codehaus.cargo.container.tomcat.internal.Tomcat4xConfigurationBuilder;

/**
 * StandAloneLocalConfiguration that is appropriate for Tomcat 4.x containers.
 * 
 * @version $Id$
 */
public class Tomcat4xStandaloneLocalConfiguration extends
    AbstractCatalinaStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * 
     * @see AbstractCatalinaStandaloneLocalConfiguration#AbstractCatalinaStandaloneLocalConfiguration(String)
     */
    public Tomcat4xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     * 
     * @see Tomcat4xConfigurationBuilder
     */
    @Override
    protected ConfigurationBuilder createConfigurationBuilder(
        LocalContainer container)
    {
        return new Tomcat4xConfigurationBuilder();
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCatalinaStandaloneLocalConfiguration#setupManager(org.codehaus.cargo.container.LocalContainer)
     */
    @Override
    protected void setupManager(LocalContainer container)
    {
        String from = ((InstalledLocalContainer) container).getHome();
        String to = getHome();
        getFileHandler().copyDirectory(from + "/server/webapps/manager",
            to + "/server/webapps/manager");
        getFileHandler().copyFile(from + "/webapps/manager.xml", to + "/webapps/manager.xml");
    }

    /**
     * {@inheritDoc} Adds the transaction manager into the set of resources assigned to this
     * configuration.
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
        return "//Engine/DefaultContext";
    }

    /**
     * {@inheritDoc}
     * 
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "Tomcat 4.x Standalone Configuration";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getOrCreateResourceConfigurationFile(Resource rs, LocalContainer container)
    {
        String confDir = getFileHandler().createDirectory(getHome(), "conf");
        return getFileHandler().append(confDir, "server.xml");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        // Tomcat 4.x does not load $CATALINA_BASE/common/lib,
        // see http://tomcat.apache.org/tomcat-4.1-doc/class-loader-howto.html

        if (container instanceof InstalledLocalContainer)
        {
            InstalledLocalContainer installedContainer = (InstalledLocalContainer) container;
            Set<String> classPath = new TreeSet<String>();
            String[] extraClasspath = installedContainer.getExtraClasspath();
            if (extraClasspath != null)
            {
                classPath.addAll(Arrays.asList(extraClasspath));
            }
            String[] sharedClasspath = installedContainer.getSharedClasspath();
            if (sharedClasspath != null)
            {
                classPath.addAll(Arrays.asList(sharedClasspath));
            }

            if (!classPath.isEmpty())
            {
                extraClasspath = new String[0];
                installedContainer.setExtraClasspath(extraClasspath);

                sharedClasspath = new String[classPath.size()];
                sharedClasspath = classPath.toArray(sharedClasspath);
                installedContainer.setSharedClasspath(sharedClasspath);
            }
        }

        super.doConfigure(container);
    }

}
