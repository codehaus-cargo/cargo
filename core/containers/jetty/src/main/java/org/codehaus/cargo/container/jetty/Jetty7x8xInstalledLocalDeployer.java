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

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.deployable.WAR;

/**
 * A deployer for webapps that deploys to a Jetty 7.x or 8.x installed instance.
 */
public class Jetty7x8xInstalledLocalDeployer extends Jetty6xInstalledLocalDeployer
{
    /**
     * {@inheritDoc}
     * @see Jetty6xInstalledLocalDeployer#Jetty6xInstalledLocalDeployer(org.codehaus.cargo.container.LocalContainer)
     */
    public Jetty7x8xInstalledLocalDeployer(LocalContainer container)
    {
        super(container);
    }

    @Override
    protected String createContextXml(WAR war)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\"  encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE Configure PUBLIC \"-//Jetty//Configure//EN\" "
            + "\"http://www.eclipse.org/jetty/configure.dtd\">\n");
        sb.append("<Configure class=\"org.eclipse.jetty.webapp.WebAppContext\">\n");
        sb.append("  <Set name=\"contextPath\">/" + war.getContext() + "</Set>\n");
        sb.append("  <Set name=\"war\">" + war.getFile() + "</Set>\n");
        sb.append("  <Set name=\"extractWAR\">true</Set>\n");
        sb.append("  <Set name=\"defaultsDescriptor\"><SystemProperty name=\"config.home\" "
            + "default=\".\"/>/etc/webdefault.xml</Set>\n");
        sb.append(getExtraClasspathXmlFragment(war));
        sb.append(getSharedClasspathXmlFragment());
        sb.append("</Configure>\n");
        return sb.toString();
    }

}
