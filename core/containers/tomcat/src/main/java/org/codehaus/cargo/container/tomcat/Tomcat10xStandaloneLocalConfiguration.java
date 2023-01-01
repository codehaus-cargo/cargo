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
package org.codehaus.cargo.container.tomcat;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.tomcat.internal.Tomcat10xConfigurationBuilder;
import org.codehaus.cargo.util.XmlReplacement.ReplacementBehavior;

/**
 * Catalina standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration}
 * implementation.
 */
public class Tomcat10xStandaloneLocalConfiguration extends Tomcat9xStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see Tomcat9xStandaloneLocalConfiguration#Tomcat9xStandaloneLocalConfiguration(String)
     */
    public Tomcat10xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        configurationBuilder = new Tomcat10xConfigurationBuilder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performXmlReplacements(LocalContainer container)
    {
        if (getPropertyValue(TomcatPropertySet.CONNECTOR_KEY_STORE_FILE) != null)
        {
            String certificateXpath = connectorXpath() + "/SSLHostConfig/Certificate";
            addXmlReplacement("conf/server.xml", certificateXpath,
                "certificateKeystoreFile", TomcatPropertySet.CONNECTOR_KEY_STORE_FILE,
                    ReplacementBehavior.ADD_MISSING_NODES);
            if (getPropertyValue(TomcatPropertySet.CONNECTOR_KEY_STORE_PASSWORD) != null)
            {
                addXmlReplacement("conf/server.xml", certificateXpath,
                    "certificateKeystoreType", TomcatPropertySet.CONNECTOR_KEY_STORE_TYPE,
                        ReplacementBehavior.ADD_MISSING_NODES);
            }
            if (getPropertyValue(TomcatPropertySet.CONNECTOR_KEY_STORE_PASSWORD) != null)
            {
                addXmlReplacement("conf/server.xml", certificateXpath,
                    "certificateKeystorePassword", TomcatPropertySet.CONNECTOR_KEY_STORE_PASSWORD,
                        ReplacementBehavior.ADD_MISSING_NODES);
            }
        }

        super.performXmlReplacements(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "Tomcat 10.x Standalone Configuration";
    }
}
