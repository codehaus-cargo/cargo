/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
package org.codehaus.cargo.container.wildfly.internal;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.script.ScriptCommand;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.container.wildfly.internal.util.WildFlyModuleUtils;
import org.codehaus.cargo.container.wildfly.internal.util.WildFlyUserUtils;
import org.codehaus.cargo.util.CargoException;

/**
 * Base class for WildFly standalone local configuration.
 */
public abstract class AbstractWildFlyStandaloneLocalConfiguration
    extends AbstractStandaloneLocalConfiguration implements WildFlyConfiguration
{

    /**
     * Configured modules - next modules will be dependent on them.
     * */
    private List<String> modules = new ArrayList<String>();

    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public AbstractWildFlyStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(JBossPropertySet.CONFIGURATION, "standalone");
        setProperty(JBossPropertySet.DEPLOYER_KEEP_ORIGINAL_WAR_FILENAME, "false");
        setProperty(JBossPropertySet.ALTERNATIVE_MODULES_DIR, "modules");
        setProperty(JBossPropertySet.JBOSS_AJP_PORT, "8009");
        setProperty(JBossPropertySet.JBOSS_HTTPS_PORT, "8443");
        setProperty(JBossPropertySet.JBOSS_MANAGEMENT_HTTP_PORT, "9990");
        setProperty(JBossPropertySet.JBOSS_MANAGEMENT_HTTPS_PORT, "9993");
    }

    /**
     * {@inheritDoc}. Ignore port offset and configure, see
     * <a href="https://codehaus-cargo.atlassian.net/browse/CARGO-1415">CARGO-1415
     * (<code>cargo.port.offset</code> sets the port offset twice)</a> for details.
     */
    @Override
    public void configure(LocalContainer container)
    {
        boolean portOffsetApplied = isOffsetApplied(ServletPropertySet.PORT);
        try
        {
            if (portOffsetApplied)
            {
                revertPortOffset();
            }
            super.configure(container);
        }
        finally
        {
            if (portOffsetApplied)
            {
                applyPortOffset();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer c) throws Exception
    {
        InstalledLocalContainer container = (InstalledLocalContainer) c;

        setupConfigurationDir();

        // Copy initial configuration
        String initialConfiguration = getFileHandler().append(container.getHome(), "standalone");
        getFileHandler().copyDirectory(initialConfiguration, getHome());

        String configurationXmlFile = "configuration/"
                + getPropertyValue(JBossPropertySet.CONFIGURATION) + ".xml";
        String configurationXML = getFileHandler().append(getHome(), configurationXmlFile);
        if (!getFileHandler().exists(configurationXML))
        {
            throw new CargoException("Missing configuration XML file: " + configurationXML);
        }

        getLogger().info("Configuring JBoss using the ["
            + getPropertyValue(JBossPropertySet.CONFIGURATION) + "] server configuration",
                this.getClass().getName());

        // Set user properties.
        createMgmtUsersProperties();
        createApplicationUsersProperties();
        createApplicationRolesProperties();
    }

    /**
     * Create module from JAR file and add it to the script list.
     * @param jarFile JAR resource representing module.
     * @param container Container with modules.
     * @param configurationScript List of configuration scripts to be executed.
     */
    protected void addModuleScript(String jarFile, InstalledLocalContainer container,
        List<ScriptCommand> configurationScript)
    {
        String moduleName = WildFlyModuleUtils.getModuleName(container, jarFile);
        boolean isModuleDeployed = WildFlyModuleUtils.isModuleDeployed(container, jarFile);

        if (isModuleDeployed)
        {
            getLogger().warn("Module " + moduleName + " already exists, skipping it.",
                    this.getClass().getName());
        }
        else
        {
            // Dependencies needed for DataSource driver initialization.
            List<String> commonDependencies = Arrays.asList("javax.api", "javax.transaction.api");
            List<String> dependencies = new ArrayList<String>();
            dependencies.addAll(commonDependencies);
            dependencies.addAll(modules);

            configurationScript.add(getConfigurationFactory().addModuleScript(moduleName,
                    Arrays.asList(jarFile), dependencies));
        }

        modules.add(moduleName);
    }

    /**
     * Create management users file (mgmt-users.properties).
     */
    private void createMgmtUsersProperties()
    {
        // Add token filters for authenticated users
        if (!getUsers().isEmpty())
        {
            StringBuilder managementToken = new StringBuilder(
                "# JBoss mgmt-users.properties file generated by CARGO\n");

            for (User user : getUsers())
            {
                managementToken.append(WildFlyUserUtils.generateUserPasswordLine(
                    user, "ManagementRealm"));
            }

            getFileHandler().writeTextFile(
                getFileHandler().append(getHome(), "/configuration/mgmt-users.properties"),
                    managementToken.toString(), StandardCharsets.ISO_8859_1);
        }
    }

    /**
     * Create application users file (application-users.properties).
     */
    private void createApplicationUsersProperties()
    {
        // Add token filters for authenticated users
        if (!getUsers().isEmpty())
        {
            StringBuilder usersToken = new StringBuilder(
                    "# JBoss application-users.properties file generated by CARGO\n");

            for (User user : getUsers())
            {
                usersToken.append(WildFlyUserUtils.generateUserPasswordLine(
                    user, "ApplicationRealm"));
            }

            getFileHandler().writeTextFile(
                getFileHandler().append(
                    getHome(), "/configuration/application-users.properties"),
                        usersToken.toString(), StandardCharsets.ISO_8859_1);
        }
    }

    /**
     * Create application roles file (application-roles.properties).
     */
    private void createApplicationRolesProperties()
    {
        // Add token filters for authenticated users
        if (!getUsers().isEmpty())
        {
            StringBuilder rolesToken = new StringBuilder(
                "# JBoss application-roles.properties file generated by CARGO\n");

            for (User user : getUsers())
            {
                rolesToken.append(user.getName());
                rolesToken.append("=");
                rolesToken.append(String.join(",", user.getRoles()));
                rolesToken.append('\n');
            }

            getFileHandler().writeTextFile(
                getFileHandler().append(
                    getHome(), "/configuration/application-roles.properties"),
                        rolesToken.toString(), StandardCharsets.ISO_8859_1);
        }
    }
}
