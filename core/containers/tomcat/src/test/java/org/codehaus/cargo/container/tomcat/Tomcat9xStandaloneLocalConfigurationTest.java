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
package org.codehaus.cargo.container.tomcat;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * Tests for the Tomcat 9 implementation of StandaloneLocalConfigurationTest
 */
public class Tomcat9xStandaloneLocalConfigurationTest extends
    Tomcat8xStandaloneLocalConfigurationTest
{

    /**
     * Creates a {@link Tomcat9xStandaloneLocalConfiguration}. {@inheritDoc}
     * @param home Configuration home.
     * @return Local configuration for <code>home</code>.
     */
    @Override
    protected LocalConfiguration createLocalConfiguration(String home)
    {
        return new Tomcat9xStandaloneLocalConfiguration(home)
        {
            @Override
            protected void setupConfFiles(String confDir)
            {
                setupManager(container);
            }

            @Override
            protected void configureFiles(FilterChain filterChain, LocalContainer container)
            {
                createServerXml();
                super.configureFiles(filterChain, container);
            }
        };
    }

    /**
     * Creates a {@link Tomcat9xInstalledLocalContainer}. {@inheritDoc}
     * @param configuration Container's configuration.
     * @return Local container for <code>configuration</code>.
     */
    @Override
    protected InstalledLocalContainer createLocalContainer(LocalConfiguration configuration)
    {
        return new Tomcat9xInstalledLocalContainer(configuration);
    }
}
