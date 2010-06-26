/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
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

/**
 * Oc4j9x standalone configuration implementation.
 *
 * @version $Id$
 */
public class Oc4j9xStandaloneLocalConfiguration extends AbstractOrionStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.orion.internal.AbstractOrionStandaloneLocalConfiguration#AbstractOrionStandaloneLocalConfiguration(String)
     */
    public Oc4j9xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     * @see AbstractOrionStandaloneLocalConfiguration#copyCustomResources(java.io.File, org.apache.tools.ant.types.FilterChain)
     */
    @Override
    protected void copyCustomResources(String confDir, FilterChain filterChain) throws Exception
    {
        getResourceUtils().copyResource(RESOURCE_PATH + "oc4j9x" + "/global-web-application.xml",
            getFileHandler().append(confDir, "global-web-application.xml"), getFileHandler(),
            filterChain);
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "OC4J Standalone Configuration";
    }

}
