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
package org.codehaus.cargo.maven2;

import java.io.File;

import junit.framework.TestCase;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.maven2.configuration.ArtifactInstaller;
import org.codehaus.cargo.maven2.configuration.Configuration;
import org.codehaus.cargo.maven2.configuration.Container;
import org.codehaus.cargo.maven2.configuration.ZipUrlInstaller;
import org.codehaus.cargo.maven2.log.MavenLogger;
import org.codehaus.cargo.util.log.FileLogger;
import org.codehaus.cargo.util.log.Logger;

/**
 * Unit tests for {@link AbstractCargoMojo}.
 * 
 * @version $Id$
 */
public class CargoMojoTest extends TestCase
{
    /**
     * {@link AbstractCargoMojo} for testing.
     */
    public class TestableAbstractCargoMojo extends AbstractCargoMojo
    {
        /**
         * {@inheritDoc}
         * @see AbstractCargoMojo#doExecute()
         */
        @Override
        public void doExecute() throws MojoExecutionException
        {
            this.createContainer();
        }
    }

    /**
     * {@link AbstractCargoMojo} for testing.
     */
    private TestableAbstractCargoMojo mojo;

    /**
     * {@inheritDoc}. Create the {@link AbstractCargoMojo} for testing.
     */
    @Override
    protected void setUp()
    {
        this.mojo = new TestableAbstractCargoMojo();
    }

    /**
     * Test the calculation of container artifact IDs.
     */
    public void testCalculateContainerArtifactId()
    {
        assertEquals(
            "cargo-core-container-jboss",
                AbstractCargoMojo.calculateContainerArtifactId("jboss42x"));
        assertEquals(
            "cargo-core-container-oc4j",
                AbstractCargoMojo.calculateContainerArtifactId("oc4j10x"));
    }

    /**
     * Test logger creation when a log element is specified.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateLoggerWhenLogElementSpecified() throws Exception
    {
        // Create temporary log file for the test
        File logFile = File.createTempFile("cargo-test-" + getClass().getName(), ".log");
        logFile.deleteOnExit();

        this.mojo.setContainerElement(new Container());
        this.mojo.getContainerElement().setLog(logFile);

        Logger logger = this.mojo.createLogger();
        assertEquals(FileLogger.class.getName(), logger.getClass().getName());
    }

    /**
     * Test logger creation when no log element is specified.
     */
    public void testCreateLoggerWhenLogElementNotSpecified()
    {
        Logger logger = this.mojo.createLogger();
        assertEquals(MavenLogger.class.getName(), logger.getClass().getName());
    }

    /**
     * Test the replacement with absolute directories.
     * @throws Exception If anything goes wrong.
     */
    public void testAbsoluteDirectoryReplacement() throws Exception
    {
        this.mojo.setContainerElement(new Container());
        this.mojo.getContainerElement().setHome("home");
        this.mojo.getContainerElement().setContainerId("dummy-container");
        this.mojo.setConfigurationElement(new Configuration());
        this.mojo.getConfigurationElement().setHome("configuration-home");
        this.mojo.getContainerElement().setZipUrlInstaller(new ZipUrlInstaller());
        this.mojo.getContainerElement().getZipUrlInstaller().setDownloadDir("downlad-dir");
        this.mojo.getContainerElement().getZipUrlInstaller().setExtractDir("extract-dir");
        this.mojo.getContainerElement().setArtifactInstaller(new ArtifactInstaller());
        this.mojo.getContainerElement().getArtifactInstaller().setExtractDir("artifact-dir");

        assertFalse("Container home is already absolute",
            new File(this.mojo.getContainerElement().getHome()).isAbsolute());
        assertFalse("Container configuration home is already absolute",
            new File(this.mojo.getConfigurationElement().getHome()).isAbsolute());
        assertFalse("Zip URL installer download directory is already absolute", new File(
            this.mojo.getContainerElement().getZipUrlInstaller().getDownloadDir()).isAbsolute());
        assertFalse("Zip URL installer extract directory is already absolute", new File(
            this.mojo.getContainerElement().getZipUrlInstaller().getExtractDir()).isAbsolute());
        assertFalse("Artifact installer extract directory is already absolute", new File(
            this.mojo.getContainerElement().getArtifactInstaller().getExtractDir()).isAbsolute());

        try
        {
            this.mojo.doExecute();
        }
        catch (ContainerException e)
        {
            // This is expected to fail since there is no container called dummy-container
            assertTrue(
                "Exception message [" + e.getMessage() + "] doesn't contain dummy-container",
                    e.getMessage().contains("dummy-container"));
        }

        assertTrue("Container home is not absolute",
            new File(this.mojo.getContainerElement().getHome()).isAbsolute());
        assertTrue("Container configuration home is not absolute",
            new File(this.mojo.getConfigurationElement().getHome()).isAbsolute());
        assertTrue("Zip URL installer download directory is not absolute", new File(
            this.mojo.getContainerElement().getZipUrlInstaller().getDownloadDir()).isAbsolute());
        assertTrue("Zip URL installer extract directory is not absolute", new File(
            this.mojo.getContainerElement().getZipUrlInstaller().getExtractDir()).isAbsolute());
        assertTrue("Artifact installer extract directory is not absolute", new File(
            this.mojo.getContainerElement().getArtifactInstaller().getExtractDir()).isAbsolute());
    }
}
