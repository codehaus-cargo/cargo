/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2020 Ali Tokmen.
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

import java.nio.charset.StandardCharsets;
import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.orion.internal.AbstractOrionStandaloneLocalConfiguration;

/**
 * Oc4j9x standalone configuration implementation.
 */
public class Oc4j9xStandaloneLocalConfiguration extends AbstractOrionStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see AbstractOrionStandaloneLocalConfiguration#AbstractOrionStandaloneLocalConfiguration(String)
     */
    public Oc4j9xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void copyCustomResources(String confDir, FilterChain filterChain) throws Exception
    {
        getResourceUtils().copyResource(RESOURCE_PATH + "oc4j9x" + "/global-web-application.xml",
            getFileHandler().append(confDir, "global-web-application.xml"), getFileHandler(),
                filterChain, StandardCharsets.UTF_8);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "OC4J Standalone Configuration";
    }

}
