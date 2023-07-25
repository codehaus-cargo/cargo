/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.util.CargoException;

/**
 * A deployer for webapps that deploys to a Jetty 12.x installed instance.
 */
public class Jetty12xInstalledLocalDeployer extends Jetty9x10x11xInstalledLocalDeployer
{
    /**
     * Supported EE versions.
     */
    private static final Map<String, String> SUPPORTED_EE_VERSIONS =
        new HashMap<String, String>(3)
            {{
                put("ee8", "");
                put("ee9", "_9_0");
                put("ee10", "_10_0");
            }};

    /**
     * {@inheritDoc}
     * @see Jetty9x10x11xInstalledLocalDeployer#Jetty9x10x11xInstalledLocalDeployer(org.codehaus.cargo.container.LocalContainer)
     */
    public Jetty12xInstalledLocalDeployer(LocalContainer container)
    {
        super(container);
    }

    @Override
    protected String createContextXml(WAR war)
    {
        String eeVersion =
            getContainer().getConfiguration().getPropertyValue(JettyPropertySet.EE_VERSION);
        String eeConfigure = Jetty12xInstalledLocalDeployer.SUPPORTED_EE_VERSIONS.get(eeVersion);
        if (eeConfigure == null)
        {
            throw new CargoException("Specified EE version is invalid. Possible values: "
                + Jetty12xInstalledLocalDeployer.SUPPORTED_EE_VERSIONS.values());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\"  encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE Configure PUBLIC \"-//Jetty//Configure//EN\" "
            + "\"http://www.eclipse.org/jetty/configure" + eeConfigure + ".dtd\">\n");
        sb.append(
            "<Configure class=\"org.eclipse.jetty." + eeVersion + ".webapp.WebAppContext\">\n");
        sb.append("  <Set name=\"contextPath\">/" + war.getContext() + "</Set>\n");
        sb.append("  <Set name=\"war\">" + war.getFile() + "</Set>\n");
        sb.append("  <Set name=\"extractWAR\">true</Set>\n");
        sb.append("  <Set name=\"defaultsDescriptor\"><SystemProperty name=\"config.home\" "
            + "default=\".\"/>/etc/webdefault-" + eeVersion + ".xml</Set>\n");
        sb.append(getExtraClasspathXmlFragment(war));
        sb.append(getSharedClasspathXmlFragment());
        sb.append("</Configure>\n");
        return sb.toString();
    }

}
