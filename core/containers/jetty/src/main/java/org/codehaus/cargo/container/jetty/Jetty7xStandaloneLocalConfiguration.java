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
package org.codehaus.cargo.container.jetty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.jetty.internal.Jetty7x8x9xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.JettyUtils;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;

/**
 * Jetty 7.x standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 */
public class Jetty7xStandaloneLocalConfiguration extends
    Jetty6xStandaloneLocalConfiguration
{
    /**
     * Capability of the Jetty 7.x standalone local configuration.
     */
    private static final ConfigurationCapability CAPABILITY =
        new Jetty7x8x9xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see Jetty7xStandaloneLocalConfiguration#Jetty7xStandaloneLocalConfiguration(String)
     */
    public Jetty7xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
        setProperty(JettyPropertySet.REALM_NAME, "Cargo Test Realm");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doConfigure(LocalContainer container) throws Exception
    {
        super.doConfigure(container);

        String etcDir = getFileHandler().append(getHome(), "etc");

        Map<String, String> jettyXmlReplacements = new HashMap<String, String>();
        jettyXmlReplacements.put("<Property", "<SystemProperty");

        if (getUsers() != null && !getUsers().isEmpty())
        {
            JettyUtils.createRealmFile(getUsers(), etcDir, getFileHandler());
            jettyXmlReplacements.put("</Configure>", configureRealmXml() + "</Configure>");
        }

        if (getDataSources() != null && !getDataSources().isEmpty())
        {
            configureDatasource(container, etcDir);
        }

        getFileHandler().replaceInFile(getFileHandler().append(etcDir, "jetty.xml"),
            jettyXmlReplacements, StandardCharsets.UTF_8);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractCopyingInstalledLocalDeployer createDeployer(
        InstalledLocalContainer container)
    {
        Jetty7x8xInstalledLocalDeployer deployer = new Jetty7x8xInstalledLocalDeployer(container);
        return deployer;
    }

    /**
     * Configure the XML portions for the authentication realm.
     * @return XML portions for the authentication realm.
     */
    protected String configureRealmXml()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<Call name=\"addBean\">\n");
        sb.append("  <Arg>\n");
        sb.append("    <New class=\"org.eclipse.jetty.security.HashLoginService\">\n");
        sb.append("      <Set name=\"name\">"
            + getPropertyValue(JettyPropertySet.REALM_NAME) + "</Set>\n");
        sb.append("      <Set name=\"config\">"
            + "<SystemProperty name=\"config.home\" default=\".\"/>"
                + "/etc/cargo-realm.properties</Set>\n");
        sb.append("    </New>\n");
        sb.append("  </Arg>\n");
        sb.append("</Call>\n");
        return sb.toString();
    }

    /**
     * Configure datasource definitions.
     * @param container Container.
     * @param etcDir The <code>etc</code> directory of the configuration.
     * @throws IOException If the pooling component cannot be copied.
     */
    protected void configureDatasource(LocalContainer container, String etcDir) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<Call name=\"setAttribute\">\n");
        sb.append("  <Arg>org.eclipse.jetty.webapp.configuration</Arg>\n");
        sb.append("  <Arg>\n");
        sb.append("    <Array type=\"java.lang.String\">\n");
        sb.append("      <Item>org.eclipse.jetty.webapp.WebInfConfiguration</Item>\n");
        sb.append("      <Item>org.eclipse.jetty.webapp.WebXmlConfiguration</Item>\n");
        sb.append("      <Item>org.eclipse.jetty.webapp.MetaInfConfiguration</Item>\n");
        sb.append("      <Item>org.eclipse.jetty.webapp.FragmentConfiguration</Item>\n");
        sb.append("      <Item>org.eclipse.jetty.plus.webapp.EnvConfiguration</Item>\n");
        if (container.getName().startsWith("Jetty 7.1."))
        {
            sb.append("      <Item>org.eclipse.jetty.plus.webapp.Configuration</Item>\n");
        }
        else
        {
            sb.append("      <Item>org.eclipse.jetty.plus.webapp.PlusConfiguration</Item>\n");
        }
        sb.append("      <Item>org.eclipse.jetty.webapp.JettyWebXmlConfiguration</Item>\n");
        sb.append("      <Item>org.eclipse.jetty.webapp.TagLibConfiguration</Item>\n");
        sb.append("    </Array>\n");
        sb.append("  </Arg>\n");
        sb.append("</Call>\n");

        createDatasourceDefinitions(sb, container);

        Map<String, String> jettyXmlReplacements = new HashMap<String, String>();
        jettyXmlReplacements.put("</Configure>", sb.toString() + "</Configure>");
        getFileHandler().replaceInFile(getFileHandler().append(etcDir, "jetty.xml"),
            jettyXmlReplacements, StandardCharsets.UTF_8);
    }

    /**
     * Creates datasource definitions.
     * @param sb String buffer to print the definitions into.
     * @param container Container.
     * @throws IOException If the pooling component cannot be copied.
     */
    protected void createDatasourceDefinitions(StringBuilder sb, LocalContainer container)
        throws IOException
    {
        // Add datasources
        for (DataSource ds : getDataSources())
        {
            sb.append("\n");
            sb.append("<New id=\"" + ds.getId()
                + "\" class=\"org.eclipse.jetty.plus.jndi.Resource\">\n");
            sb.append("  <Arg>" + ds.getJndiLocation() + "</Arg>\n");
            sb.append("  <Arg>\n");
            sb.append("    <New class=\"com.mchange.v2.c3p0.ComboPooledDataSource\">\n");
            sb.append("      <Set name=\"driverClass\">" + ds.getDriverClass() + "</Set>\n");
            sb.append("      <Set name=\"jdbcUrl\">" + ds.getUrl() + "</Set>\n");
            sb.append("      <Set name=\"user\">" + ds.getUsername() + "</Set>\n");
            sb.append("      <Set name=\"password\">" + ds.getPassword() + "</Set>\n");
            sb.append("    </New>\n");
            sb.append("  </Arg>\n");
            sb.append("</New>\n");
        }

        String mchangeCommonsFile = getFileHandler().append(getHome(),
            "lib/ext/mchange-commons-java.jar");
        try (InputStream mchangeCommonsReader = getClass().getClassLoader().getResourceAsStream(
            "org/codehaus/cargo/container/jetty/datasource/mchange-commons-java.jar");
            OutputStream mchangeCommonsWriter =
                getFileHandler().getOutputStream(mchangeCommonsFile))
        {
            getFileHandler().copy(mchangeCommonsReader, mchangeCommonsWriter);
        }

        String c3p0File = getFileHandler().append(getHome(), "lib/ext/c3p0.jar");
        try (InputStream c3p0Reader = getClass().getClassLoader().getResourceAsStream(
            "org/codehaus/cargo/container/jetty/datasource/c3p0.jar");
            OutputStream c3p0Writer = getFileHandler().getOutputStream(c3p0File))
        {
            getFileHandler().copy(c3p0Reader, c3p0Writer);
        }

        InstalledLocalContainer installedContainer = (InstalledLocalContainer) container;
        installedContainer.addExtraClasspath(mchangeCommonsFile);
        installedContainer.addExtraClasspath(c3p0File);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "Jetty 7.x Standalone Configuration";
    }
}
