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
package org.codehaus.cargo.container.deployable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.spi.deployable.AbstractDeployablewithSettableName;
import org.codehaus.cargo.module.application.ApplicationXml;
import org.codehaus.cargo.module.application.DefaultEarArchive;
import org.codehaus.cargo.module.application.EarArchive;

/**
 * Wraps an EAR file that will be deployed in the container.
 */
public class EAR extends AbstractDeployablewithSettableName
{
    /**
     * List of webapps that have been found during parsing inside the wrapped EAR.
     */
    private Map<String, String> webapps;

    /**
     * @param ear the location of the EAR being wrapped.
     */
    public EAR(String ear)
    {
        super(ear);
    }

    /**
     * Parse the EAR to find out the web apps it contains.
     */
    private void parseWebApps()
    {
        if (this.webapps == null)
        {
            Map<String, String> webapps = new HashMap<String, String>();
            try
            {
                EarArchive ear = new DefaultEarArchive(getFile());
                ApplicationXml applicationXml = ear.getApplicationXml();
                for (String webUri : applicationXml.getWebModuleUris())
                {
                    String context = applicationXml.getWebModuleContextRoot(webUri);

                    if (context == null)
                    {
                        // The application.xml does not define a <context-root>
                        // element. This is wrong!
                        throw new ContainerException("Your application.xml must define a "
                            + "<context-root> element in the <web> module definition.");
                    }

                    // Remove leading "/" if there is one.
                    if (context.startsWith("/"))
                    {
                        context = context.substring(1);
                    }

                    getLogger().debug("Found Web URI [" + webUri + "], context [" + context + "]",
                        this.getClass().getName());

                    webapps.put(context, webUri);
                }
            }
            catch (Exception e)
            {
                throw new ContainerException("Failed to parse webapps from [" + getFile()
                    + "] EAR.", e);
            }
            this.webapps = webapps;
        }
    }

    /**
     * @return the list of Web contexts of all WAR files contained in the wrapped EAR
     */
    public synchronized List<String> getWebContexts()
    {
        parseWebApps();
        return new ArrayList<String>(this.webapps.keySet());
    }

    /**
     * @return the list of Web URIs of all WAR files contained in the wrapped EAR
     */
    public synchronized List<String> getWebUris()
    {
        parseWebApps();
        return new ArrayList<String>(this.webapps.values());
    }

    /**
     * @param context the context for which we want to find out the web URI
     * @return the web URI for the WAR matching the context passed as parameter
     */
    public synchronized String getWebUri(String context)
    {
        parseWebApps();
        return this.webapps.get(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeployableType getType()
    {
        return DeployableType.EAR;
    }
}
