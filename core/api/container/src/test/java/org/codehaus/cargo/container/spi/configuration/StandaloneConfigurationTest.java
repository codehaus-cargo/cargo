/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.container.spi.configuration;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.LoggingLevel;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.util.VFSFileHandler;

/**
 * Unit tests for {@link AbstractStandaloneLocalConfiguration}.
 */
public class StandaloneConfigurationTest
{

    /**
     * Mock {@link AbstractStandaloneLocalConfiguration} implementation.
     */
    public class TestableAbstractStandaloneConfiguration
        extends AbstractStandaloneLocalConfiguration
    {
        /**
         * {@inheritDoc}
         * @param dir Configuration directory.
         */
        public TestableAbstractStandaloneConfiguration(String dir)
        {
            super(dir);
        }

        /**
         * Doesn't do anything. {@inheritDoc}
         * @param container Ignored.
         */
        @Override
        protected void doConfigure(LocalContainer container)
        {
            // Do nothing voluntarily for testing
        }

        /**
         * {@inheritDoc}
         * @return Mock {@link ConfigurationCapability}.
         */
        @Override
        public ConfigurationCapability getCapability()
        {
            return new ConfigurationCapability()
            {
                /**
                 * {@inheritDoc}
                 * @return <code>false</code>.
                 */
                @Override
                public boolean supportsProperty(String propertyName)
                {
                    return false;
                }

                /**
                 * {@inheritDoc}
                 * @return {@link Collections#emptyMap()}
                 */
                @Override
                public Map<String, Boolean> getProperties()
                {
                    return Collections.emptyMap();
                }
            };
        }
    }

    /**
     * Test the creation of a config directory when the target directory does not exist yet.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testCreateConfigDirWhenDirectoryDoesNotExist() throws Exception
    {
        String configDir = "ram:///cargo/testCreateConfigDirWhenDirectoryDoesNotExist";

        FileObject configDirObject = VFS.getManager().resolveFile(configDir);
        FileObject timestampFileObject = configDirObject.resolveFile(".cargo");

        configDirObject.delete(new AllFileSelector());

        TestableAbstractStandaloneConfiguration configuration =
            new TestableAbstractStandaloneConfiguration(configDir);
        configuration.setFileHandler(new VFSFileHandler());
        configuration.setupConfigurationDir();

        Assertions.assertTrue(configDirObject.exists(), "Config dir should have been created");
        Assertions.assertTrue(timestampFileObject.exists(), "Cargo timestamp should have existed");
    }

    /**
     * Test the creation of a config directory when the target directory exists and is empty.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testCreateConfigDirWhenDirectoryExistButIsEmpty() throws Exception
    {
        String configDir = "ram:///cargo/testCreateConfigDirWhenDirectoryExistButIsEmpty";

        FileObject configDirObject = VFS.getManager().resolveFile(configDir);
        FileObject timestampFileObject = configDirObject.resolveFile(".cargo");

        configDirObject.createFolder();

        TestableAbstractStandaloneConfiguration configuration =
            new TestableAbstractStandaloneConfiguration(configDir);
        configuration.setFileHandler(new VFSFileHandler());
        configuration.setupConfigurationDir();

        Assertions.assertTrue(timestampFileObject.exists(), "Cargo timestamp should have existed");
    }

    /**
     * Test the creation of a config directory when the target directory exists and is not empty.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testCreateConfigDirWhenDirectoryNotEmpty() throws Exception
    {
        String configDir = "ram:///cargo/testCreateConfigDirWhenDirectoryNotEmpty";

        FileObject configDirObject = VFS.getManager().resolveFile(configDir);
        configDirObject.resolveFile("somefile").createFile();

        TestableAbstractStandaloneConfiguration configuration =
            new TestableAbstractStandaloneConfiguration(configDir);
        configuration.setFileHandler(new VFSFileHandler());

        try
        {
            configuration.setupConfigurationDir();
            Assertions.fail(
                "Should have thrown a ContainerException as the directory is not empty");
        }
        catch (ContainerException expected)
        {
            Assertions.assertEquals("Invalid configuration dir "
                + "[ram:///cargo/testCreateConfigDirWhenDirectoryNotEmpty]. When using standalone "
                + "configurations, the configuration dir must point to an empty directory - "
                + "Except if the configuration was created by Cargo.", expected.getMessage());
        }
    }

    /**
     * Test the setting of default properties.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testDefaultPropertiesAreSet() throws Exception
    {
        TestableAbstractStandaloneConfiguration configuration =
            new TestableAbstractStandaloneConfiguration("dummy");

        Assertions.assertEquals("8080", configuration.getPropertyValue(ServletPropertySet.PORT));
        Assertions.assertEquals(LoggingLevel.MEDIUM.getLevel(),
            configuration.getPropertyValue(GeneralPropertySet.LOGGING));
        Assertions.assertEquals(
            "localhost", configuration.getPropertyValue(GeneralPropertySet.HOSTNAME));
    }

    /**
     * Test the setting of extra properties in addition to the default properties.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testSetPropertyWhenDefaultPropertyExists() throws Exception
    {
        TestableAbstractStandaloneConfiguration configuration =
            new TestableAbstractStandaloneConfiguration("dummy");

        configuration.setProperty(ServletPropertySet.PORT, "8081");
        Assertions.assertEquals("8081", configuration.getPropertyValue(ServletPropertySet.PORT));
    }

}
