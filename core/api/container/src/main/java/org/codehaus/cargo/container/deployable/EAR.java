/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
import org.codehaus.cargo.container.spi.deployable.AbstractDeployable;
import org.codehaus.cargo.module.application.ApplicationXml;
import org.codehaus.cargo.module.application.DefaultEarArchive;
import org.codehaus.cargo.module.application.EarArchive;

/**
 * Wraps an EAR file that will be deployed in the container.
 */
public class EAR extends AbstractDeployable
{
    /**
     * The name of this deployable (it can be anything, there's no special rule). If not specified
     * by user, it is computed from the EAR's file name (removing the filename extension).
     */
    private String name;

    /**
     * List of webapps that have been found during parsing inside the wrapped EAR.
     */
    private Map<String, String> webapps;

    /**
     * {@inheritDoc}
     * @see AbstractDeployable#AbstractDeployable(String)
     */
    public EAR(String ear)
    {
        super(ear);
    }

    /**
     * Parse the EAR file name to set up the EAR name. The parsing occurs only if the user has not
     * already specified a custom name.
     * 
     * @see #setName(String)
     */
    private void parseName()
    {
        if (this.name == null)
        {
            String name = getFileHandler().getName(getFile());
            int nameIndex = name.toLowerCase().lastIndexOf(".ear");
            if (nameIndex >= 0)
            {
                name = name.substring(0, nameIndex);
            }

            getLogger().debug("Parsed EAR name = [" + name + "]", this.getClass().getName());

            setName(name);
        }
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
     * Sets the name of this deployable. It can be anything (there's no special rule).
     * @param name the name of this deployable
     */
    public synchronized void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns the name of this deployable. If not specified by user, it is computed from the
     * EAR's file name (removing the filename extension).
     * @return the name of this deployable
     */
    @Override
    public synchronized String getName()
    {
        parseName();
        return this.name;
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
