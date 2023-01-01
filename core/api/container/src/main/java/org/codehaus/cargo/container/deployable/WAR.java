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
package org.codehaus.cargo.container.deployable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.cargo.container.spi.deployable.AbstractDeployable;
import org.codehaus.cargo.util.DefaultFileHandler;

/**
 * Wraps a WAR file that will be deployed in the container. The root context for this WAR is taken
 * from the name of the WAR file (without the extension).
 */
public class WAR extends AbstractDeployable
{
    /**
     * The web context parsed from the name of the WAR file.
     */
    private String context;

    /**
     * Additional classpath entries for the web application that usually reside outside of the WAR
     * file to facilitate rapid development without fully assembling the WAR file.
     */
    private List<String> extraClasspath;

    /**
     * @param war the location of the WAR being wrapped. This must point to either a WAR file or an
     * expanded WAR directory.
     */
    public WAR(String war)
    {
        super(war);
        this.extraClasspath = new ArrayList<String>();
    }

    /**
     * @param context the context name to use when deploying this WAR. If not specified by the user,
     * then the default context name is computed from the name of WAR itself (without the file
     * extension).
     */
    public synchronized void setContext(String context)
    {
        // Ensure the context is well-formed by removing any extra leading or ending "/".
        String wellFormedContext = context.trim();
        while (wellFormedContext.contains("//"))
        {
            wellFormedContext = wellFormedContext.replace("//", "/");
        }
        while (wellFormedContext.startsWith("/"))
        {
            wellFormedContext = wellFormedContext.substring(1);
        }
        while (wellFormedContext.endsWith("/"))
        {
            wellFormedContext = wellFormedContext.substring(0, wellFormedContext.length() - 1);
        }
        this.context = wellFormedContext;
    }

    /**
     * Extract the context name from the WAR file name (without the file extension). For example if
     * the WAR is named <code>test.war</code> then the context name is <code>test</code>.
     */
    private void parseContext()
    {
        if (this.context == null)
        {
            String ctx = getFileHandler().getName(getFile());
            int warIndex = ctx.toLowerCase().lastIndexOf(".war");
            if (warIndex >= 0)
            {
                ctx = ctx.substring(0, warIndex);
            }

            getLogger().debug("Parsed web context = [" + ctx + "]", this.getClass().getName());

            setContext(ctx);
        }
    }

    /**
     * @return the context name, either the computed name derived from the WAR file name or the name
     * defined by the user. Note that this method doesn't return any leading "/" before the context
     * name.
     */
    public synchronized String getContext()
    {
        parseContext();
        return this.context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeployableType getType()
    {
        return DeployableType.WAR;
    }

    /**
     * Sets additional classpath entries for the web application that usually reside outside of the
     * WAR file to facilitate rapid development without fully assembling the WAR file. In general,
     * this feature is meant for use with exploded WARs and as such is usually not supported for
     * non-expanded WARs. If this method is use with tomcat container, the configuration must set
     * TomcatPropertySet#COPY_WARS to false
     * 
     * @param classpath The additional classpath entries for the web application, may be
     *            {@code null}.
     */
    public synchronized void setExtraClasspath(String[] classpath)
    {
        this.extraClasspath.clear();
        if (classpath != null)
        {
            Collections.addAll(this.extraClasspath, classpath);
        }
    }

    /**
     * Gets additional classpath entries for the web application that usually reside outside of the
     * WAR file to facilitate rapid development without fully assembling the WAR file.
     * 
     * @return The additional classpath entries for the web application, never {@code null}.
     */
    public synchronized String[] getExtraClasspath()
    {
        return this.extraClasspath.toArray(new String[this.extraClasspath.size()]);
    }

    /**
     * Returns the name of this deployable. For WAR file it is its context.
     * @return the name of this deployable
     */
    @Override
    public String getName()
    {
        return getContext();
    }

    /**
     * Similar to {@link #getFilename()}, without the <code>.war</code> extension.
     * @return The file or directory name for this WAR, taking into account the context.
     */
    public String getBaseFilename()
    {
        String baseFilename = this.getContext();

        // First check for ROOT here, before sanitizing, in case the / path was set "normally"
        if (baseFilename == null || baseFilename.trim().isEmpty() || baseFilename.matches("/+"))
        {
            getLogger().info(
                "The WAR file has its context set to / and will therefore be "
                    + "deployed as ROOT.war", this.getClass().getName());
            baseFilename = "ROOT";
        }
        else
        {
            baseFilename = DefaultFileHandler.sanitizeFilename(baseFilename, getLogger());
        }

        // Also check for ROOT after sanitizing file name
        if (baseFilename.trim().isEmpty() || baseFilename.matches("/+"))
        {
            getLogger().info(
                "The WAR file has its sanitized context set to [" + baseFilename
                    + "] and will therefore be deployed as ROOT.war", this.getClass().getName());
            baseFilename = "ROOT";
        }

        return baseFilename;
    }

    /**
     * {@inheritDoc}<br>
     * <br>
     * Value is calculated based on the WAR context and whether it is expanded or not.
     */
    @Override
    public String getFilename()
    {
        String filename = getBaseFilename();
        if (!this.isExpanded())
        {
            filename = filename + ".war";
        }
        return filename;
    }
}
