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
package org.codehaus.cargo.container.tomcat;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.tomcat.internal.TomcatUtils;
import org.codehaus.cargo.util.CargoException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Catalina standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration}
 * implementation.
 */
public class Tomcat6xStandaloneLocalConfiguration extends Tomcat5xStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see Tomcat5xStandaloneLocalConfiguration#Tomcat5xStandaloneLocalConfiguration(String)
     */
    public Tomcat6xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        super.doConfigure(container);

        // CARGO-1272: Starting Tomcat generates warnings on not existing folders in classloader
        getFileHandler().createDirectory(getHome(), "common/classes");
        getFileHandler().createDirectory(getHome(), "shared/classes");
        getFileHandler().createDirectory(getHome(), "shared/lib");
    }

    /**
     * {@inheritDoc}
     *
     * the path to find the manager application is different between v5 and v6.
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
            getFileHandler().copyDirectory(from + "/webapps/manager",
                to + "/webapps/manager");
            getFileHandler().copyDirectory(from + "/webapps/host-manager",
                to + "/webapps/host-manager");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> getCatalinaPropertertiesReplacements()
    {
        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("common.loader=",
            "common.loader=${catalina.base}/common/classes,${catalina.base}/common/lib/*.jar,");
        replacements.put("shared.loader=",
            "shared.loader=${catalina.base}/shared/classes,${catalina.base}/shared/lib/*.jar,");
        return replacements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getExtraClasspathToken(WAR deployable)
    {
        String extraClasspath = TomcatUtils.getExtraClasspath(deployable, true);
        if (extraClasspath != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("<Loader");
            sb.append(" className=\"org.apache.catalina.loader.VirtualWebappLoader\"");
            sb.append(" virtualClasspath=\"" + extraClasspath + "\"");
            sb.append("/>");
            return sb.toString();
        }
        else
        {
            return "";
        }
    }

    @Override
    protected void configureExtraClasspathToken(WAR deployable, Element context)
    {
        String extraClasspath = TomcatUtils.getExtraClasspath(deployable, true);
        if (extraClasspath != null)
        {
            NodeList loaderList = context.getElementsByTagName("Loader");
            Element loader;
            if (loaderList.getLength() > 0)
            {
                loader = (Element) loaderList.item(0);
            }
            else
            {
                loader = context.getOwnerDocument().createElement("Loader");
                context.appendChild(loader);
            }

            String className = loader.getAttribute("className");
            if (className != null && !className.isEmpty()
                && !"org.apache.catalina.loader.WebappLoader".equals(className)
                && !"org.apache.catalina.loader.VirtualWebappLoader".equals(className))
            {
                throw new CargoException("Extra classpath is not supported"
                    + " for WARs using custom loader: " + className);
            }
            loader.setAttribute("className", "org.apache.catalina.loader.VirtualWebappLoader");

            String virtualClasspath = loader.getAttribute("virtualClasspath");
            if (virtualClasspath == null || virtualClasspath.isEmpty())
            {
                virtualClasspath = extraClasspath;
            }
            else
            {
                virtualClasspath = extraClasspath + ";" + virtualClasspath;
            }
            loader.setAttribute("virtualClasspath", virtualClasspath);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performXmlReplacements(LocalContainer container)
    {
        addXmlReplacement("conf/server.xml", connectorXpath(), "SSLEnabled",
            TomcatPropertySet.HTTP_SECURE);
        String connectorProtocolClass = getPropertyValue(
            TomcatPropertySet.CONNECTOR_PROTOCOL_CLASS);
        if (connectorProtocolClass != null)
        {
            addXmlReplacement("conf/server.xml", connectorXpath(), "protocol",
                connectorProtocolClass);
        }

        super.performXmlReplacements(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "Tomcat 6.x Standalone Configuration";
    }
}
