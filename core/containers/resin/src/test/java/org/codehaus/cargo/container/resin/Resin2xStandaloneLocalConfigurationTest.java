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
package org.codehaus.cargo.container.resin;

import java.nio.charset.StandardCharsets;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.resin.internal.AbstractResinStandaloneLocalConfigurationTest;
import org.codehaus.cargo.container.resin.internal.Resin2xConfigurationChecker;

/**
 * Unit tests for {@link Resin2xStandaloneLocalConfiguration}.
 */
public class Resin2xStandaloneLocalConfigurationTest extends
    AbstractResinStandaloneLocalConfigurationTest
{

    /**
     * Creates a {@link Resin2xStandaloneLocalConfiguration}. {@inheritDoc}
     * @param home Configuration home.
     * @return Local configuration for <code>home</code>.
     */
    @Override
    protected LocalConfiguration createLocalConfiguration(String home)
    {
        return new Resin2xStandaloneLocalConfiguration(home);
    }

    /**
     * Creates a {@link Resin2xInstalledLocalContainer}. {@inheritDoc}
     * @param configuration Container's configuration.
     * @return Local container for <code>configuration</code>.
     */
    @Override
    protected InstalledLocalContainer createLocalContainer(LocalConfiguration configuration)
    {
        return new Resin2xInstalledLocalContainer(configuration);
    }

    /**
     * @return {@link Resin2xConfigurationChecker}
     */
    protected ConfigurationChecker createDataSourceConfigurationChecker()
    {
        return new Resin2xConfigurationChecker();
    }

    /**
     * Set up datasource file. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUpDataSourceFile() throws Exception
    {
        String file = configuration.getHome() + "/conf/resin.conf";
        getFileHandler().writeTextFile(file, "<caucho.com/>", StandardCharsets.ISO_8859_1);
    }

    /**
     * @return {@link Resin2xConfigurationChecker}.
     */
    @Override
    protected ConfigurationChecker createConfigurationChecker()
    {
        return new Resin2xConfigurationChecker();
    }

}
