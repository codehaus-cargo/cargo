/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.module.webapp.tomcat.TomcatWarArchive;

/**
 * Extension that supports custom Tomcat <code>context.xml</code> files located 
 * in the <code>META-INF/</code> directory of your WAR. For example, this allows
 * returning the right web context even if it has been defined in the 
 * <code>context.xml</code> file.
 * 
 * @version $Id$
 */
public class TomcatWAR extends WAR
{
    /**
     * The parsed Tomcat descriptors in the WAR.
     */
    private TomcatWarArchive warArchive;
    
    /**
     * @param war the location of the WAR being wrapped. This must point to either a WAR file or an
     *        expanded WAR directory.
     */
    public TomcatWAR(String war)
    {
        super(war);
        
        try
        {
            this.warArchive = new TomcatWarArchive(getFile());
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to parse Tomcat WAR file "
                + "in [" + getFile() + "]", e);
        }
    }

    /**
     * @return the context defined in <code>context.xml</code> if any.
     *         If there is no <code>context.xml</code> or if it doesn't
     *         define any root context, then return {@link WAR#getContext()}.
     */
    @Override
    public synchronized String getContext()
    {
        String result = parseTomcatContextXml();
        if (result == null)
        {
            result = super.getContext();
        }
        
        return result;
    }
    
    /**
     * @return the context from Tomcat's <code>context.xml</code> if
     *         it is defined or <code>null</code> otherwise.
     */
    private String parseTomcatContextXml()
    {
        String context = null;
        
        if (this.warArchive.getTomcatContextXml() != null)
        {
            context = this.warArchive.getTomcatContextXml().getPath();
        }
        
        return context;
    }   

    /**
     * @return true if the WAR contains a <code>META-INF/context.xml</code>
     *         file
     */
    public boolean containsContextFile()
    {
        return (this.warArchive.getTomcatContextXml() != null);
    }
}
