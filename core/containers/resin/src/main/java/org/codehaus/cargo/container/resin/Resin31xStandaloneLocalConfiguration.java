/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
package org.codehaus.cargo.container.resin;

import java.io.IOException;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.property.ServletPropertySet;

/**
 * Resin 3.1.x standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 * 
 */
public class Resin31xStandaloneLocalConfiguration extends Resin3xStandaloneLocalConfiguration
{

    /**
     * {@inheritDoc}
     * 
     * @see Resin3xStandaloneLocalConfiguration#Resin3xStandaloneLocalConfiguration(String)
     */
    public Resin31xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     * 
     * @see Resin3xStandaloneLocalConfiguration#prepareConfigurationDirectory(org.codehaus.cargo.container.Container, java.lang.String)
     */
    @Override
    protected void prepareConfigurationDirectory(Container container, String confDir)
        throws IOException
    {
        super.prepareConfigurationDirectory(container, confDir);

        removeXmlReplacement("conf/resin.conf", "//resin/server/http", "port");
        addXmlReplacement("conf/resin.conf", "//resin/cluster/server-default/http", "port",
            ServletPropertySet.PORT);
    }

}
