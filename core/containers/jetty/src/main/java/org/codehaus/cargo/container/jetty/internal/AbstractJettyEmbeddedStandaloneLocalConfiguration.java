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
package org.codehaus.cargo.container.jetty.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jetty.JettyPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;

/**
 * Base class for Jetty standalone configurations.
 */
public abstract class AbstractJettyEmbeddedStandaloneLocalConfiguration extends
    AbstractStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public AbstractJettyEmbeddedStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(JettyPropertySet.USE_FILE_MAPPED_BUFFER, "true");
        setProperty(JettyPropertySet.DEPLOYER_CREATE_CONTEXT_XML, "true");
        setProperty(JettyPropertySet.REALM_NAME, "Cargo Test Realm");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract ConfigurationCapability getCapability();

    /**
     * {@inheritDoc}
     */
    @Override
    public void doConfigure(LocalContainer container) throws Exception
    {
        try
        {
            EmbeddedLocalContainer embeddedContainer = (EmbeddedLocalContainer) container;

            setupConfigurationDir();

            String etcDir = getFileHandler().createDirectory(getHome(), "etc");
            getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
                new File(getHome(), "cargocpc.war"));
            String webdefault = getFileHandler().append(etcDir, "webdefault.xml");
            getFileHandler().createFile(webdefault);
            InputStream webdefaultReader = null;
            try
            {
                webdefaultReader = embeddedContainer.getClassLoader().getResourceAsStream(
                    "org/mortbay/jetty/servlet/webdefault.xml");
                if (webdefaultReader == null)
                {
                    webdefaultReader = embeddedContainer.getClassLoader().getResourceAsStream(
                        "org/mortbay/jetty/webapp/webdefault.xml");
                }
                if (webdefaultReader == null)
                {
                    webdefaultReader = embeddedContainer.getClassLoader().getResourceAsStream(
                        "org/eclipse/jetty/webapp/webdefault.xml");
                }
                if (webdefaultReader == null)
                {
                    throw new FileNotFoundException("Cannot find the webdefault.xml file");
                }
                try (OutputStream webdefaultWriter = getFileHandler().getOutputStream(webdefault))
                {
                    getFileHandler().copy(webdefaultReader, webdefaultWriter);
                }
            }
            finally
            {
                if (webdefaultReader != null)
                {
                    webdefaultReader.close();
                }
                webdefaultReader = null;
            }

            if (container.getOutput() != null)
            {
                activateLogging(container);
            }
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to create a " + container.getName()
                + " container configuration", e);
        }
    }

    /**
     * Turn on the logging for the container.
     * @param container the container for which to establish logging
     * @throws Exception on error
     */
    protected abstract void activateLogging(LocalContainer container)
        throws Exception;
}
