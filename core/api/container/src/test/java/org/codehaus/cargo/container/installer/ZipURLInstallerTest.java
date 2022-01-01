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
package org.codehaus.cargo.container.installer;

import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;

/**
 * Unit tests for {@link ZipURLInstaller}.
 */
public class ZipURLInstallerTest extends TestCase
{
    /**
     * Installer.
     */
    private ZipURLInstaller installer;

    /**
     * Whether to make the {@link ZipURLInstaller#doDownload()} method fail.
     */
    private boolean doDownloadFail;

    /**
     * Proxy set when {@link ZipURLInstaller#doDownload()} was called.
     */
    private String doDownloadProxyHost;

    /**
     * File system manager.
     */
    private StandardFileSystemManager fsManager;

    /**
     * File handler.
     */
    private FileHandler fileHandler;

    /**
     * Creates the test ZIP URL installer and its fils system manager. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);

        this.installer = new ZipURLInstaller(new URL("http://some/url/resin-3.0.18.zip"))
        {
            @Override
            protected void doDownload() throws IOException
            {
                ZipURLInstallerTest.this.doDownloadProxyHost =
                    System.getProperty("http.proxyHost");
                if (ZipURLInstallerTest.this.doDownloadFail)
                {
                    ZipURLInstallerTest.this.doDownloadFail = false;
                    throw new IOException();
                }
            }
        };
        this.installer.setFileHandler(this.fileHandler);
    }

    /**
     * Closes the test file system manager. {@inheritDoc}
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
     * Test {@link ZipURLInstaller#getExtractDir()}.
     * @throws Exception If anything goes wrong.
     */
    public void testGetExtractDir() throws Exception
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
        assertNull("No system proxy should be set", System.getProperty("http.proxyHost"));

        Proxy proxy = new Proxy();
        proxy.setHost("proxyhost");
        this.installer.setProxy(proxy);

        this.doDownloadFail = false;
        this.installer.download();

        assertEquals(this.doDownloadProxyHost, proxy.getHost());
        assertNull("System proxy host should be unset", System.getProperty("http.proxyHost"));
    }

    /**
     * Test {@link ZipURLInstaller#install()} successful with no proxy.
     * @throws Exception If anything goes wrong.
     */
    public void testSuccessfulDownloadWhenNoProxySet() throws Exception
    {
        assertNull("No system proxy should be set", System.getProperty("http.proxyHost"));

        this.doDownloadFail = false;
        this.installer.download();

        assertNull(this.doDownloadProxyHost);
        assertNull("No system proxy should be set", System.getProperty("http.proxyHost"));
    }

    /**
     * Test {@link ZipURLInstaller#install()} failed with proxy and then successful without proxy.
     * @throws Exception If anything goes wrong.
     */
    public void testFailureWithProxySetButSuccessOnSecondTryWithoutProxy() throws Exception
    {
        assertNull("No system proxy should be set", System.getProperty("http.proxyHost"));

        Proxy proxy = new Proxy();
        proxy.setHost("proxyhost");
        this.installer.setProxy(proxy);

        this.doDownloadFail = true;
        this.installer.download();

        assertNull(
            "First failure should have resulted in the proxy being unset before second attempt",
                this.doDownloadProxyHost);
        assertNull("System proxy host should be unset", System.getProperty("http.proxyHost"));
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
