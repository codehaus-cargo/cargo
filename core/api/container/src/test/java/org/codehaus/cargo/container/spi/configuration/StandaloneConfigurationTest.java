/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.VFS;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.util.VFSFileHandler;

/**
 * Unit tests for {@link AbstractStandaloneLocalConfiguration}.
 * 
 * @version $Id$
 */
public class StandaloneConfigurationTest extends TestCase
{
    public class TestableAbstractStandaloneConfiguration
        extends AbstractStandaloneLocalConfiguration
    {
        public TestableAbstractStandaloneConfiguration(String dir)
        {
            super(dir);
        }

        @Override
        protected void doConfigure(LocalContainer container)
        {
            // Do nothing voluntarily for testing
        }

        public ConfigurationCapability getCapability()
        {
            return new ConfigurationCapability()
            {
                public boolean supportsProperty(String propertyName)
                {
                    return false;
                }

                public Map getProperties()
                {
                    return new HashMap();
                }
            };
        }
    }

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

        assertTrue("Config dir should have been created", configDirObject.exists());
        assertTrue("Cargo timestamp should have existed", timestampFileObject.exists());
    }

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

        assertTrue("Cargo timestamp should have existed", timestampFileObject.exists());
    }

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
            fail("Should have thrown a ContainerException as the directory is not empty");
        }
        catch (ContainerException expected)
        {
            assertEquals("Invalid configuration dir "
                + "[ram:///cargo/testCreateConfigDirWhenDirectoryNotEmpty]. When using standalone "
                + "configurations, the configuration dir must point to an empty directory. Note "
                + "that everything in that dir will get deleted by Cargo.", expected.getMessage());
        }
    }

    public void testDefaultPropertiesAreSet() throws Exception
    {
        TestableAbstractStandaloneConfiguration configuration =
            new TestableAbstractStandaloneConfiguration("dummy");

        assertEquals("8080", configuration.getPropertyValue(ServletPropertySet.PORT));
        assertEquals("medium", configuration.getPropertyValue(GeneralPropertySet.LOGGING));
        assertEquals("localhost", configuration.getPropertyValue(GeneralPropertySet.HOSTNAME));
    }

    public void testSetPropertyWhenDefaultPropertyExists() throws Exception
    {
        TestableAbstractStandaloneConfiguration configuration =
            new TestableAbstractStandaloneConfiguration("dummy");

        configuration.setProperty(ServletPropertySet.PORT, "8081");
        assertEquals("8081", configuration.getPropertyValue(ServletPropertySet.PORT));
    }

}
