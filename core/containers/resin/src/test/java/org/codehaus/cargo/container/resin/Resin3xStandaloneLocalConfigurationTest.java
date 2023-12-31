/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.resin.internal.AbstractResinStandaloneLocalConfigurationTest;
import org.codehaus.cargo.container.resin.internal.Resin3xConfigurationChecker;

/**
 * Unit tests for {@link Resin3xStandaloneLocalConfiguration}.
 */
public class Resin3xStandaloneLocalConfigurationTest extends
    AbstractResinStandaloneLocalConfigurationTest
{

    /**
     * Creates a {@link Resin3xStandaloneLocalConfiguration}. {@inheritDoc}
     * @param home Configuration home.
     * @return Local configuration for <code>home</code>.
     */
    @Override
    protected LocalConfiguration createLocalConfiguration(String home)
    {
        return new Resin3xStandaloneLocalConfiguration(home);
    }

    /**
     * Creates a {@link Resin3xInstalledLocalContainer}. {@inheritDoc}
     * @param configuration Container's configuration.
     * @return Local container for <code>configuration</code>.
     */
    @Override
    protected InstalledLocalContainer createLocalContainer(LocalConfiguration configuration)
    {
        return new Resin3xInstalledLocalContainer(configuration);
    }

    /**
     * Call parent and check that the XML file is here. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void testConfigure() throws Exception
    {
        getFileHandler().createFile(container.getHome() + "/conf/app-default.xml");

        getFileHandler().delete(container.getHome() + "/conf/resin.conf");
        getFileHandler().createFile(container.getHome() + "/conf/resin.conf");
        OutputStream resinConf = null;
        InputStream originalResinConf = null;
        try
        {
            resinConf =
                getFileHandler().getOutputStream(container.getHome() + "/conf/resin.conf");
            originalResinConf = getResinConfiguration();
            assertNotNull("Cannot load Resin configuration file for tests", originalResinConf);
            getFileHandler().copy(originalResinConf, resinConf);
        }
        finally
        {
            if (resinConf != null)
            {
                try
                {
                    resinConf.close();
                }
                catch (IOException ignored)
                {
                    // Ignored
                }
                resinConf = null;
            }

            if (originalResinConf != null)
            {
                try
                {
                    originalResinConf.close();
                }
                catch (IOException ignored)
                {
                    // Ignored
                }
                originalResinConf = null;
            }

            System.gc();
        }

        super.testConfigure();

        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/app-default.xml"));
    }

    /**
     * @return The Resin configuration file to use for tests.
     */
    protected InputStream getResinConfiguration()
    {
        return this.getClass().getClassLoader().getResourceAsStream("resin3x.conf");
    }

    /**
     * Set up datasource file. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUpDataSourceFile() throws Exception
    {
        String file = configuration.getHome() + "/conf/resin.conf";
        // TODO: We cannot use StandardCharsets.UTF_8 due to the javac --release 6 constraint
        getFileHandler().writeTextFile(file, "<resin xmlns=\"http://caucho.com/ns/resin\" "
            + "xmlns:resin=\"http://caucho.com/ns/resin/core\" />", Charset.forName("UTF-8"));
    }

    /**
     * @return {@link Resin3xConfigurationChecker}.
     */
    @Override
    protected ConfigurationChecker createConfigurationChecker()
    {
        return new Resin3xConfigurationChecker();
    }

}
