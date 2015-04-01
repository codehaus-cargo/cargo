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
package org.codehaus.cargo.container.jetty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.jetty.internal.AbstractJettyStandaloneLocalConfiguration;
import org.codehaus.cargo.container.jetty.internal.Jetty7xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;

/**
 * Jetty 7.x standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 * 
 */
public class Jetty7xStandaloneLocalConfiguration extends
    AbstractJettyStandaloneLocalConfiguration
{
    /**
     * Capability of the Jetty 7.x standalone local configuration.
     */
    private static ConfigurationCapability capability =
        new Jetty7xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractJettyStandaloneLocalConfiguration#AbstractJettyStandaloneLocalConfiguration(String)
     */
    public Jetty7xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    @Override
    protected FilterChain createJettyFilterChain()
    {
        FilterChain filterChain = super.createJettyFilterChain();

        String sessionPath = getPropertyValue(JettyPropertySet.SESSION_PATH);
        String sessionContextParam = "";
        if (sessionPath != null)
        {
            sessionContextParam =
                "  <context-param>\n"
                    + "    <param-name>org.eclipse.jetty.servlet.SessionPath</param-name>\n"
                    + "    <param-value>" + sessionPath + "</param-value>\n"
                    + "  </context-param>\n";
        }

        getAntUtils().addTokenToFilterChain(filterChain,
            "cargo.jetty.session.path.context-param", sessionContextParam);

        return filterChain;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        super.doConfigure(container);

        String etcDir = getFileHandler().append(getHome(), "etc");

        Map<String, String> jettyXmlReplacements = new HashMap<String, String>();
        jettyXmlReplacements.put("<Property", "<SystemProperty");

        if (getDataSources() != null && !getDataSources().isEmpty())
        {
            configureDatasource(container, etcDir);
        }

        getFileHandler().replaceInFile(
            getFileHandler().append(etcDir, "jetty.xml"), jettyXmlReplacements, "UTF-8");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractCopyingInstalledLocalDeployer createDeployer(
        InstalledLocalContainer container)
    {
        Jetty7xInstalledLocalDeployer deployer = new Jetty7xInstalledLocalDeployer(container);
        return deployer;
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
        sb.append("      <Item>org.eclipse.jetty.plus.webapp.PlusConfiguration</Item>\n");
        sb.append("      <Item>org.eclipse.jetty.webapp.JettyWebXmlConfiguration</Item>\n");
        sb.append("      <Item>org.eclipse.jetty.webapp.TagLibConfiguration</Item>\n");
        sb.append("    </Array>\n");
        sb.append("  </Arg>\n");
        sb.append("</Call>\n");

        createDatasourceDefinitions(sb, container);

        Map<String, String> jettyXmlReplacements = new HashMap<String, String>();
        jettyXmlReplacements.put("</Configure>", sb.toString() + "</Configure>");
        getFileHandler().replaceInFile(
            getFileHandler().append(etcDir, "jetty.xml"), jettyXmlReplacements, "UTF-8");
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

        InputStream c3p0Reader = getClass().getClassLoader().getResourceAsStream(
            "org/codehaus/cargo/container/jetty/datasource/c3p0.jar");
        String c3p0File = getFileHandler().append(getHome(), "lib/ext/c3p0.jar");
        OutputStream c3p0Writer = getFileHandler().getOutputStream(c3p0File);
        try
        {
            getFileHandler().copy(c3p0Reader, c3p0Writer);
        }
        finally
        {
            c3p0Writer.close();
            c3p0Writer = null;
            System.gc();
        }

        InstalledLocalContainer installedContainer = (InstalledLocalContainer) container;
        installedContainer.addExtraClasspath(c3p0File);
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "Jetty 7.x Standalone Configuration";
    }
}
