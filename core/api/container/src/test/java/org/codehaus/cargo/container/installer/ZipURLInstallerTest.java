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
package org.codehaus.cargo.container.installer;

import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Get;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.util.AntTaskFactory;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;

/**
 * Unit tests for {@link ZipURLInstaller}.
 * 
 * @version $Id$
 */
public class ZipURLInstallerTest extends TestCase
{
    /**
     * Installer.
     */
    private ZipURLInstaller installer;

    /**
     * File system manager.
     */
    private StandardFileSystemManager fsManager;

    /**
     * File handler.
     */
    private FileHandler fileHandler;

    /**
     * Dummy {@link Get} implementation that doesn't do anything.
     */
    private class HarmlessGet extends Get
    {
        /**
         * Doesn't do anything. {@inheritDoc}
         * @throws BuildException Never thrown.
         */
        @Override
        public void execute() throws BuildException
        {
            // Do nothing
        }
    }

    /**
     * Dummy {@link Get} implementation that always fails.
     */
    private class FailingGet extends Get
    {
        /**
         * Fails. {@inheritDoc}
         * @throws BuildException Always thrown.
         */
        @Override
        public void execute() throws BuildException
        {
            throw new BuildException("Failed to download file...");
        }
    }

    /**
     * Creates the test ZIP URL installer and its fils system manager. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);

        this.installer = new ZipURLInstaller(new URL("http://some/url/resin-3.0.18.zip"));
        this.installer.setFileHandler(this.fileHandler);
    }

    /**
     * Closes the test file system manager. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void tearDown() throws Exception
    {
        if (fsManager != null)
        {
            fsManager.close();
        }

        super.tearDown();
    }

    /**
     * Test {@link ZipURLInstaller#getSourceFileName()}.
     */
    public void testGetSourceFileName()
    {
        assertEquals("resin-3.0.18.zip", this.installer.getSourceFileName());
    }

    /**
     * Test {@link ZipURLInstaller#getInstallDirName()}.
     * @throws Exception If anything goes wrong.
     */
    public void testGetInstallDirName() throws Exception
    {
        assertTrue(this.installer.getExtractDir() + " does not end with " + "resin-3.0.18",
            this.installer.getExtractDir().endsWith("resin-3.0.18"));
    }

    /**
     * Test {@link ZipURLInstaller#install()} successful with a proxy.
     * @throws Exception If anything goes wrong.
     */
    public void testSuccessfulDownloadWhenProxySet() throws Exception
    {
        this.installer.setAntTaskFactory(
            new AntTaskFactory()
            {
                public Task createTask(String taskName)
                {
                    return new HarmlessGet();
                }
            });
        Proxy proxy = new Proxy();
        proxy.setHost("proxyhost");
        this.installer.setProxy(proxy);

        this.installer.download();

        assertEquals(System.getProperty("http.proxyHost"), proxy.getHost());
    }

    /**
     * Test {@link ZipURLInstaller#install()} successful with no proxy.
     * @throws Exception If anything goes wrong.
     */
    public void testSuccessfulDownloadWhenNoProxySet() throws Exception
    {
        // Clear any proxy setting
        new Proxy().clear();

        this.installer.setAntTaskFactory(
            new AntTaskFactory()
            {
                public Task createTask(String taskName)
                {
                    return new HarmlessGet();
                }
            });

        this.installer.download();

        assertNull("Proxy host should not have been set", System.getProperty("http.proxyHost"));
    }

    /**
     * Test {@link ZipURLInstaller#install()} failed with proxy and then successful without proxy.
     * @throws Exception If anything goes wrong.
     */
    public void testFailureWithProxySetButSuccessOnSecondTryWithoutProxy() throws Exception
    {
        this.installer.setAntTaskFactory(
            new AntTaskFactory()
            {
                private int count = 0;

                public Task createTask(String taskName)
                {
                    Task result;
                    if (this.count++ == 0)
                    {
                        result = new FailingGet();
                    }
                    else
                    {
                        result = new HarmlessGet();
                    }
                    return result;
                }
            });
        Proxy proxy = new Proxy();
        proxy.setHost("proxyhost");
        this.installer.setProxy(proxy);

        this.installer.download();

        assertNull("Proxy host should have been unset", System.getProperty("http.proxyHost"));
    }

    /**
     * Test {@link ZipURLInstaller#getHome()} when container not installed yet.
     * @throws Exception If anything goes wrong.
     */
    public void testGetHomeWhenContainerNotInstalled() throws Exception
    {
        this.installer.setExtractDir("ram:///tmp");
        try
        {
            this.installer.getHome();
            fail("Should have thrown a container exception here");
        }
        catch (ContainerException expected)
        {
            assertEquals("Failed to get container installation home as the container has not yet "
                + "been installed. Please call install() first.", expected.getMessage());
        }
    }

    /**
     * Test {@link ZipURLInstaller#getHome()} when container installed in two levels.
     * @throws Exception If anything goes wrong.
     */
    public void testGetHomeWhenContainerDistributionUnzipsInTwoLevels() throws Exception
    {
        this.fsManager.resolveFile("ram:///tmp/resin-3.0.18/resin-3.0.18/bin").createFolder();
        this.fsManager.resolveFile("ram:///tmp/resin-3.0.18/resin-3.0.18/lib").createFolder();
        this.fsManager.resolveFile("ram:///tmp/resin-3.0.18/resin-3.0.18/webapps").createFolder();
        this.fsManager.resolveFile("ram:///tmp/resin-3.0.18/.cargo").createFile();

        this.installer.setExtractDir("ram:///tmp");

        assertEquals("ram:///tmp/resin-3.0.18/resin-3.0.18", this.installer.getHome());
    }
}
