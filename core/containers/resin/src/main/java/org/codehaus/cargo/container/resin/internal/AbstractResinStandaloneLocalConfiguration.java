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
package org.codehaus.cargo.container.resin.internal;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.resin.ResinInstalledLocalDeployer;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder;

/**
 * Common class for all Resin standalone local configurations.
 */
public abstract class AbstractResinStandaloneLocalConfiguration extends
    AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder
{
    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder#AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder(String)
     */
    public AbstractResinStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOrCreateDataSourceConfigurationFile(DataSource ds, LocalContainer container)
    {
        return getOrCreateResourceConfigurationFile(null, container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getOrCreateResourceConfigurationFile(Resource resource,
        LocalContainer container)
    {
        String path = getFileHandler().append(getHome(), "conf");
        return getFileHandler().append(path, getResinConfigurationFileName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getXpathForDataSourcesParent()
    {
        return getXpathForResourcesParent();
    }

    /**
     * Allow specific version implementations to add custom preparation steps before the container
     * is started.
     * 
     * @param container the container to configure
     * @param confDir Resin <code>conf</code> directory
     * @exception IOException in case of an error
     */
    protected abstract void prepareConfigurationDirectory(Container container, String confDir)
        throws IOException;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupConfigurationDir();

        InstalledLocalContainer resinContainer = (InstalledLocalContainer) container;

        String confDir = getFileHandler().createDirectory(getHome(), "conf");

        prepareConfigurationDirectory(container, confDir);

        String webappsDir = getFileHandler().createDirectory(getHome(), "webapps");

        // Deploy the deployables
        ResinInstalledLocalDeployer deployer = new ResinInstalledLocalDeployer(resinContainer);
        deployer.deploy(getDeployables());

        // Deploy the CPC (Cargo Ping Component) to the webapps directory.
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            getFileHandler().append(webappsDir, "cargocpc.war"), getFileHandler());
    }

    /**
     * @param dirAttribute name of attribute used in <code>resin.conf</code> to specify where the
     * war is located. This Attribute is different for different versions of Resin.
     * @return the value for the <code>resin3x.expanded.webapps</code> filter token
     */
    protected String createExpandedWarTokenValue(String dirAttribute)
    {
        // Note: An Ant filter token cannot be an empty string hence the single
        // character used for initializing the StringBuilder.
        StringBuilder expandedWarValue = new StringBuilder(" ");
        for (Deployable deployable : getDeployables())
        {
            if (deployable.getType() == DeployableType.WAR)
            {
                if (deployable.isExpanded())
                {
                    // Note: No need to create these directories as Resin will
                    // do it for us.
                    File tmp = new File(getHome(), "tmp/" + ((WAR) deployable).getContext());
                    File work = new File(getHome(), "work/" + ((WAR) deployable).getContext());

                    expandedWarValue.append("<web-app id='");
                    expandedWarValue.append(((WAR) deployable).getContext());
                    expandedWarValue.append("' " + dirAttribute + "='");
                    expandedWarValue.append(((WAR) deployable).getFile());
                    expandedWarValue.append("'><temp-dir>");
                    expandedWarValue.append(tmp.getPath());
                    expandedWarValue.append("</temp-dir><work-dir>");
                    expandedWarValue.append(work.getPath());
                    expandedWarValue.append("</work-dir></web-app>");
                }
            }
        }
        return expandedWarValue.toString();
    }

    /**
     * @param prefix the prefix string to use for each user
     * @param suffix the suffix string to use for each user
     * @return an Ant filter token containing all the user-defined users
     */
    protected String getSecurityToken(String prefix, String suffix)
    {
        StringBuilder token = new StringBuilder(" ");

        // Add token filters for authenticated users
        if (!getUsers().isEmpty())
        {
            for (User user : getUsers())
            {
                token.append(prefix);
                token.append(user.getName());
                token.append(':');
                token.append(user.getPassword());
                token.append(':');

                Iterator<String> roles = user.getRoles().iterator();
                while (roles.hasNext())
                {
                    String role = roles.next();
                    token.append(role);
                    if (roles.hasNext())
                    {
                        token.append(',');
                    }
                }
                token.append(suffix);
            }
        }

        return token.toString();
    }

    /**
     * @return Resin configuration file name.
     */
    protected String getResinConfigurationFileName()
    {
        return "resin.conf";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "Resin Standalone Configuration";
    }

}
