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
package org.codehaus.cargo.container.jetty;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;

/**
 * Jetty 12.x standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 */
public class Jetty12xStandaloneLocalConfiguration extends Jetty11xStandaloneLocalConfiguration
{
    /**
     * All <code>webdefault-*.xml</code> files from Jetty 12.x.
     */
    private static final List<String> WEBDEFAULT_XML_FILES =
        Arrays.asList("webdefault-ee8.xml", "webdefault-ee9.xml", "webdefault-ee10.xml");

    /**
     * {@inheritDoc}
     * @see Jetty9xStandaloneLocalConfiguration#Jetty9xStandaloneLocalConfiguration(String)
     */
    public Jetty12xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
        setProperty(JettyPropertySet.MODULES, Jetty12xInstalledLocalContainer.DEFAULT_MODULES);
        setProperty(JettyPropertySet.DEPLOYER_EE_VERSION,
            Jetty12xInstalledLocalContainer.DEFAULT_DEPLOYER_EE_VERSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getWebdefaultFiles()
    {
        return Jetty12xStandaloneLocalConfiguration.WEBDEFAULT_XML_FILES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractCopyingInstalledLocalDeployer createDeployer(
        InstalledLocalContainer container)
    {
        Jetty12xInstalledLocalDeployer deployer = new Jetty12xInstalledLocalDeployer(container);
        return deployer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String configureRealmXml()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<Call id=\"ResourceFactory\" "
            + "class=\"org.eclipse.jetty.util.resource.ResourceFactory\" name=\"of\">\n");
        sb.append("  <Arg><Ref refid=\"Server\" /></Arg>\n");
        sb.append("  <Call id=\"realmResource\" name=\"newResource\">\n");
        sb.append("    <Arg>\n");
        sb.append("      <Property name=\"cargo.realm\" "
            + "default=\"etc/cargo-realm.properties\" />\n");
        sb.append("    </Arg>\n");
        sb.append("  </Call>\n");
        sb.append("</Call>\n\n");
        sb.append("<Call name=\"addBean\">\n");
        sb.append("  <Arg>\n");
        sb.append("    <New class=\"org.eclipse.jetty.security.HashLoginService\">\n");
        sb.append("      <Set name=\"name\">"
            + getPropertyValue(JettyPropertySet.REALM_NAME) + "</Set>\n");
        sb.append("      <Set name=\"config\"><Ref refid=\"realmResource\"/></Set>\n");
        sb.append("    </New>\n");
        sb.append("  </Arg>\n");
        sb.append("</Call>\n");
        return sb.toString();
    }

    /**
     * Jetty 12.x requiring different <code>jetty-plus</code> modules for different EE versions,
     * this implementation creates no replacements, but rather adds the <code>c3p0</code> and
     * other JARs to the container classpath. {@inheritDoc}
     */
    @Override
    protected void configureDatasource(LocalContainer container, String etcDir) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        createDatasourceDefinitions(sb, container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "Jetty 12.x Standalone Configuration";
    }

}
