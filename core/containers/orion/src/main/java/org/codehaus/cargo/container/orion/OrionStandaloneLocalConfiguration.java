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
package org.codehaus.cargo.container.orion;

import org.codehaus.cargo.container.orion.internal.AbstractOrionStandaloneLocalConfiguration;
import org.apache.tools.ant.types.FilterChain;

import java.io.File;

/**
 * Orion standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} 
 * implementation.
 *  
 * @version $Id$
 */
public class OrionStandaloneLocalConfiguration extends AbstractOrionStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see AbstractOrionStandaloneLocalConfiguration#AbstractOrionStandaloneLocalConfiguration(String)
     */
    public OrionStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     * @see AbstractOrionStandaloneLocalConfiguration#copyCustomResources(java.io.File, org.apache.tools.ant.types.FilterChain)
     */
    protected void copyCustomResources(File confDir, FilterChain filterChain) throws Exception
    {
        getResourceUtils().copyResource(RESOURCE_PATH + "orion1x2x" + "/global-web-application.xml",
            new File(confDir, "global-web-application.xml"), filterChain);
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    public String toString()
    {
        return "Orion Standalone Configuration";
    }
}
