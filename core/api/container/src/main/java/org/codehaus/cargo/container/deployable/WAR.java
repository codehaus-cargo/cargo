/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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

import org.codehaus.cargo.container.spi.deployable.AbstractDeployable;

/**
 * Wraps a WAR file that will be deployed in the container. The root context for this WAR is taken 
 * from the name of the WAR file (without the extension).
 * 
 * @version $Id$
 */
public class WAR extends AbstractDeployable
{
    /**
     * The web context parsed from the name of the WAR file. 
     */
    private String context;

    /**
     * @param war the location of the WAR being wrapped. This must point to either a WAR file or an
     *        expanded WAR directory.
     */
    public WAR(String war)
    {
        super(war);
    }

    /**
     * @param context the context name to use when deploying this WAR. If not specified by the user,
     *        then the default context name is computed from the name of WAR itself (without the 
     *        file extension).
     */
    public synchronized void setContext(String context)
    {
        // Ensure the context is also well-formed by removing any extra leading "/".
        this.context = context.replace('/', ' ').trim();
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
     * @return the context name, either the computed name derived from the WAR file name or the 
     *         name defined by the user. Note that this method doesn't return any leading "/"
     *         before the context name.
     */
    public synchronized String getContext()
    {
        parseContext();
        return this.context;
    }
    
    /**
     * @return true if the WAR file specified is pointing to an expanded WAR directory, false 
     *         otherwise 
     */
    public boolean isExpandedWar()
    {
        return getFileHandler().isDirectory(getFile());
    }

    /**
     * {@inheritDoc}
     * @see Deployable#getType()
     */
    public DeployableType getType()
    {
        return DeployableType.WAR;
    }
}
