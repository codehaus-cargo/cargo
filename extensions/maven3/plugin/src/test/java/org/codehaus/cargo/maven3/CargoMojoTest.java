/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
public class CargoMojoTest
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
     * Create the {@link AbstractCargoMojo} for testing.
     */
    @BeforeEach
    protected void setUp()
    {
        this.mojo = new TestableAbstractCargoMojo();
    }

    /**
     * Test the calculation of container artifact IDs.
     */
    @Test
    public void testCalculateContainerArtifactId()
    {
        Assertions.assertEquals(
            "cargo-core-container-jboss",
                AbstractCargoMojo.calculateContainerArtifactId("jboss42x"));
        Assertions.assertEquals(
            "cargo-core-container-oc4j",
                AbstractCargoMojo.calculateContainerArtifactId("oc4j10x"));
        Assertions.assertEquals(
            "cargo-core-container-liberty",
                AbstractCargoMojo.calculateContainerArtifactId("liberty"));
    }

    /**
     * Test the calculation of container artifacts.
     */
    @Test
    public void testCalculateContainerArtifact()
    {
        try
        {
            AbstractCargoMojo.calculateArtifact(
                "https://dllegacy.ow2.org/jonas/jonas4.10.9-tomcat5.5.28.tgz");
            Assertions.fail("Non Maven artifact somehow parsed");
        }
        catch (IllegalArgumentException expected)
        {
            // Expected
        }

        ArtifactInstaller installer = AbstractCargoMojo.calculateArtifact(
            "https://repo.maven.apache.org/maven2/org/eclipse/jetty/"
                + "jetty-home/11.0.23/jetty-home-11.0.23.tar.gz");
        Assertions.assertEquals("org.eclipse.jetty", installer.getGroupId());
        Assertions.assertEquals("jetty-home", installer.getArtifactId());
        Assertions.assertEquals("11.0.23", installer.getVersion());
        Assertions.assertEquals("tar.gz", installer.getType());
        Assertions.assertNull(installer.getClassifier());

        installer = AbstractCargoMojo.calculateArtifact(
            "https://repo.maven.apache.org/maven2/org/ow2/jonas/assemblies/profiles/legacy/"
                + "jonas-full/5.3.0/jonas-full-5.3.0-bin.tar.gz");
        Assertions.assertEquals(
            "org.ow2.jonas.assemblies.profiles.legacy", installer.getGroupId());
        Assertions.assertEquals("jonas-full", installer.getArtifactId());
        Assertions.assertEquals("5.3.0", installer.getVersion());
        Assertions.assertEquals("tar.gz", installer.getType());
        Assertions.assertEquals("bin", installer.getClassifier());

        installer = AbstractCargoMojo.calculateArtifact(
            "https://repo.maven.apache.org/maven2/org/apache/geronimo/assemblies/"
                + "geronimo-tomcat7-javaee6/3.0.1/geronimo-tomcat7-javaee6-3.0.1-bin.zip");
        Assertions.assertEquals("org.apache.geronimo.assemblies", installer.getGroupId());
        Assertions.assertEquals("geronimo-tomcat7-javaee6", installer.getArtifactId());
        Assertions.assertEquals("3.0.1", installer.getVersion());
        Assertions.assertEquals("zip", installer.getType());
        Assertions.assertEquals("bin", installer.getClassifier());
    }

    /**
     * Test logger creation when a log element is specified.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testCreateLoggerWhenLogElementSpecified() throws Exception
    {
        // Create temporary log file for the test
        File logFile = File.createTempFile("cargo-test-" + getClass().getName(), ".log");
        logFile.deleteOnExit();

        this.mojo.setContainerElement(new Container());
        this.mojo.getContainerElement().setLog(logFile);

        Logger logger = this.mojo.createLogger();
        Assertions.assertEquals(FileLogger.class.getName(), logger.getClass().getName());
    }

    /**
     * Test logger creation when no log element is specified.
     */
    @Test
    public void testCreateLoggerWhenLogElementNotSpecified()
    {
        Logger logger = this.mojo.createLogger();
        Assertions.assertEquals(MavenLogger.class.getName(), logger.getClass().getName());
    }

    /**
     * Test the replacement with absolute directories.
     * @throws Exception If anything goes wrong.
     */
    @Test
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

        Assertions.assertFalse(
            new File(this.mojo.getContainerElement().getHome()).isAbsolute(),
                "Container home is already absolute");
        Assertions.assertFalse(
            new File(this.mojo.getConfigurationElement().getHome()).isAbsolute(),
                "Container configuration home is already absolute");
        Assertions.assertFalse(
            new File(this.mojo.getContainerElement().getZipUrlInstaller().getDownloadDir())
                .isAbsolute(),
                "Zip URL installer download directory is already absolute");
        Assertions.assertFalse(
            new File(this.mojo.getContainerElement().getZipUrlInstaller().getExtractDir())
                .isAbsolute(),
                "Zip URL installer extract directory is already absolute");
        Assertions.assertFalse(
            new File(this.mojo.getContainerElement().getArtifactInstaller().getExtractDir())
                .isAbsolute(),
                "Artifact installer extract directory is already absolute");

        try
        {
            this.mojo.doExecute();
        }
        catch (ContainerException e)
        {
            // This is expected to fail since there is no container called dummy-container
            Assertions.assertTrue(
                e.getMessage().contains("dummy-container"),
                    "Exception message [" + e.getMessage() + "] doesn't contain dummy-container");
        }

        Assertions.assertTrue(
            new File(this.mojo.getContainerElement().getHome()).isAbsolute(),
                "Container home is not absolute");
        Assertions.assertTrue(
            new File(this.mojo.getConfigurationElement().getHome()).isAbsolute(),
                "Container configuration home is not absolute");
        Assertions.assertTrue(
            new File(this.mojo.getContainerElement().getZipUrlInstaller().getDownloadDir())
                .isAbsolute(),
                "Zip URL installer download directory is not absolute");
        Assertions.assertTrue(
            new File(this.mojo.getContainerElement().getZipUrlInstaller().getExtractDir())
                .isAbsolute(),
                "Zip URL installer extract directory is not absolute");
        Assertions.assertTrue(
            new File(this.mojo.getContainerElement().getArtifactInstaller().getExtractDir())
                .isAbsolute(),
                "Artifact installer extract directory is not absolute");
    }

    /**
     * Test the default installer element.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testDefaultInstallerElement() throws Exception
    {
        this.mojo.setContainerElement(new Container());
        this.mojo.getContainerElement().setContainerId("tomcat6x");

        Assertions.assertNull(
            this.mojo.getContainerElement().getZipUrlInstaller(),
                "Container installer already set");

        try
        {
            this.mojo.doExecute();
        }
        catch (ContainerException e)
        {
            // This is expected to fail since the Tomcat dependency hasn't been loaded
            Assertions.assertTrue(
                e.getMessage().contains("tomcat6x"),
                    "Exception message [" + e.getMessage() + "] doesn't contain tomcat6x");
        }

        Assertions.assertNotNull(
            this.mojo.getContainerElement().getZipUrlInstaller(),
                "Container installer not set");
        Assertions.assertNotNull(
            this.mojo.getContainerElement().getZipUrlInstaller().getUrl(),
                "Container installer URL not set");
    }
}
