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
package org.codehaus.cargo.maven3;

import java.io.File;
import java.util.Collections;

import junit.framework.TestCase;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.maven3.configuration.ArtifactInstaller;
import org.codehaus.cargo.maven3.configuration.Configuration;
import org.codehaus.cargo.maven3.configuration.Container;
import org.codehaus.cargo.maven3.configuration.ZipUrlInstaller;
import org.codehaus.cargo.maven3.log.MavenLogger;
import org.codehaus.cargo.maven3.util.CargoProject;
import org.codehaus.cargo.util.log.FileLogger;
import org.codehaus.cargo.util.log.Logger;

/**
 * Unit tests for {@link AbstractCargoMojo}.
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
            if (this.getCargoProject() == null)
            {
                this.setCargoProject(new CargoProject("dummy-packaging", "dummy-groupId",
                    "dummy-artifactId", "target/dummy-buildDirectory", "dummy-finalName",
                        Collections.EMPTY_SET, null));
            }

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
        assertEquals(
            "cargo-core-container-liberty",
                AbstractCargoMojo.calculateContainerArtifactId("liberty"));
    }

    /**
     * Test the calculation of container artifacts.
     */
    public void testCalculateContainerArtifact()
    {
        try
        {
            AbstractCargoMojo.calculateArtifact(
                "https://dllegacy.ow2.org/jonas/jonas4.10.9-tomcat5.5.28.tgz");
            fail("Non Maven artifact somehow parsed");
        }
        catch (IllegalArgumentException expected)
        {
            // Expected
        }

        ArtifactInstaller installer = AbstractCargoMojo.calculateArtifact(
            "https://repo.maven.apache.org/maven2/org/eclipse/jetty/"
                + "jetty-home/11.0.13/jetty-home-11.0.13.tar.gz");
        assertEquals("org.eclipse.jetty", installer.getGroupId());
        assertEquals("jetty-home", installer.getArtifactId());
        assertEquals("11.0.13", installer.getVersion());
        assertEquals("tar.gz", installer.getType());
        assertNull(installer.getClassifier());

        installer = AbstractCargoMojo.calculateArtifact(
            "https://repo.maven.apache.org/maven2/org/ow2/jonas/assemblies/profiles/legacy/"
                + "jonas-full/5.3.0/jonas-full-5.3.0-bin.tar.gz");
        assertEquals("org.ow2.jonas.assemblies.profiles.legacy", installer.getGroupId());
        assertEquals("jonas-full", installer.getArtifactId());
        assertEquals("5.3.0", installer.getVersion());
        assertEquals("tar.gz", installer.getType());
        assertEquals("bin", installer.getClassifier());

        installer = AbstractCargoMojo.calculateArtifact(
            "https://repo.maven.apache.org/maven2/org/apache/geronimo/assemblies/"
                + "geronimo-tomcat7-javaee6/3.0.1/geronimo-tomcat7-javaee6-3.0.1-bin.zip");
        assertEquals("org.apache.geronimo.assemblies", installer.getGroupId());
        assertEquals("geronimo-tomcat7-javaee6", installer.getArtifactId());
        assertEquals("3.0.1", installer.getVersion());
        assertEquals("zip", installer.getType());
        assertEquals("bin", installer.getClassifier());
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

    /**
     * Test the default installer element.
     * @throws Exception If anything goes wrong.
     */
    public void testDefaultInstallerElement() throws Exception
    {
        this.mojo.setContainerElement(new Container());
        this.mojo.getContainerElement().setContainerId("tomcat6x");

        assertNull("Container installer already set",
            this.mojo.getContainerElement().getZipUrlInstaller());

        try
        {
            this.mojo.doExecute();
        }
        catch (ContainerException e)
        {
            // This is expected to fail since the Tomcat dependency hasn't been loaded
            assertTrue(
                "Exception message [" + e.getMessage() + "] doesn't contain tomcat6x",
                    e.getMessage().contains("tomcat6x"));
        }

        assertNotNull("Container installer not set",
            this.mojo.getContainerElement().getZipUrlInstaller());
        assertNotNull("Container installer URL not set",
            this.mojo.getContainerElement().getZipUrlInstaller().getUrl());
    }
}
