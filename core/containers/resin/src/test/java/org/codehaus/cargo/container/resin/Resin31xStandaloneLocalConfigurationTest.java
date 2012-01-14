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

import java.io.InputStream;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * Unit tests for {@link Resin31xStandaloneLocalConfiguration}.
 * 
 * @version $Id$
 */
public class Resin31xStandaloneLocalConfigurationTest
    extends Resin3xStandaloneLocalConfigurationTest
{

    /**
     * Creates a {@link Resin31xStandaloneLocalConfiguration}. {@inheritdoc}
     * @param home Configuration home.
     * @return Local configuration for <code>home</code>.
     */
    @Override
    protected LocalConfiguration createLocalConfiguration(String home)
    {
        return new Resin31xStandaloneLocalConfiguration(home);
    }

    /**
     * Creates a {@link Resin31xInstalledLocalContainer}. {@inheritdoc}
     * @param configuration Container's configuration.
     * @return Local container for <code>configuration</code>.
     */
    @Override
    protected InstalledLocalContainer createLocalContainer(LocalConfiguration configuration)
    {
        return new Resin31xInstalledLocalContainer(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected InputStream getResinConfiguration()
    {
        return this.getClass().getClassLoader().getResourceAsStream("resin31x.conf");
    }
}
