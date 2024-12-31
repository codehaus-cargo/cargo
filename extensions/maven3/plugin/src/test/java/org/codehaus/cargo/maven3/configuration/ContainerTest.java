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
package org.codehaus.cargo.maven3.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import junit.framework.TestCase;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.installer.ZipURLInstaller;
import org.codehaus.cargo.container.stub.EmbeddedLocalContainerStub;
import org.codehaus.cargo.container.stub.InstalledLocalContainerStub;
import org.codehaus.cargo.container.stub.StandaloneLocalConfigurationStub;
import org.codehaus.cargo.maven3.util.CargoProject;
import org.codehaus.cargo.util.log.NullLogger;
import org.mockito.Mockito;

/**
 * Unit tests for the {@link org.codehaus.cargo.maven3.configuration.Container} class.
 */
public class ContainerTest extends TestCase
{
    /**
     * Test embedded container creation with system properties.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateEmbeddedContainerWithSystemPropertiesSet() throws Exception
    {
        org.codehaus.cargo.maven3.configuration.Container containerElement =
            setUpContainerElement(new EmbeddedLocalContainerStub());

        Map<String, String> props = new HashMap<String, String>();
        props.put("id1", "value1");
        props.put("id2", "value2");

        containerElement.setSystemProperties(props);

        File systemPropertiesFile =
            File.createTempFile(ConfigurationTest.class.getName(), ".properties");
        try
        {
            try (OutputStream outputStream = new FileOutputStream(systemPropertiesFile))
            {
                Properties properties = new Properties();
                properties.put("id2", "foobar");
                properties.put("id3", "value3");
                properties.store(outputStream, null);
            }
            containerElement.setSystemPropertiesFile(systemPropertiesFile);

            containerElement.createContainer(
                new StandaloneLocalConfigurationStub("configuration/home"),
                    new NullLogger(), createTestCargoProject("whatever"));
        }
        finally
        {
            systemPropertiesFile.delete();
        }
        props.put("id3", "value3");

        // For embedded containers, system properties get put into our own vm
        for (Map.Entry<String, String> entry : props.entrySet())
        {
            assertEquals(entry.getValue(), System.getProperty(entry.getKey()));
        }
    }

    /**
     * Test installed local container creation with system properties.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateInstalledLocalContainerWithSystemPropertiesSet() throws Exception
    {
        org.codehaus.cargo.maven3.configuration.Container containerElement =
            setUpContainerElement(new InstalledLocalContainerStub());

        Map<String, String> props = new HashMap<String, String>();
        props.put("id1", "value1");
        props.put("id2", "value2");

        containerElement.setSystemProperties(props);

        org.codehaus.cargo.container.InstalledLocalContainer container;
        File systemPropertiesFile =
            File.createTempFile(ConfigurationTest.class.getName(), ".properties");
        try
        {
            try (OutputStream outputStream = new FileOutputStream(systemPropertiesFile))
            {
                Properties properties = new Properties();
                properties.put("id2", "foobar");
                properties.put("id3", "value3");
                properties.store(outputStream, null);
            }
            containerElement.setSystemPropertiesFile(systemPropertiesFile);

            container =
                (InstalledLocalContainer) containerElement.createContainer(
                    new StandaloneLocalConfigurationStub("configuration/home"), new NullLogger(),
                        createTestCargoProject("whatever"));
        }
        finally
        {
            systemPropertiesFile.delete();
        }
        props.put("id3", "value3");

        assertEquals(props, container.getSystemProperties());
    }

    /**
     * Test embedded container creation with extra classpath dependency.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateEmbeddedContainerWithExtraClasspathDependency() throws Exception
    {
        // 1) Create a JAR file acting as an extra dependency
        String resourceValue = "file in zip in dependency";
        String resourceName = "maven-test-my-dependency.txt";

        File zipFile = File.createTempFile("maven3-plugin-test-dependency", ".zip");
        zipFile.deleteOnExit();

        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile)))
        {
            zip.putNextEntry(new ZipEntry(resourceName));
            zip.write(resourceValue.getBytes(StandardCharsets.UTF_8));
        }

        // 2) Create a Maven 3 Artifact linked to the JAR file just created
        DefaultArtifact artifact = new DefaultArtifact("customGroupId", "customArtifactId",
            VersionRange.createFromVersion("0.1"), "compile", "jar", null,
            new DefaultArtifactHandler());
        artifact.setFile(zipFile);
        Set<Artifact> artifacts = new HashSet<Artifact>();
        artifacts.add(artifact);

        // 3) Set up the container element and add the extra Dependency to it
        Dependency dependencyElement = new Dependency();
        dependencyElement.setGroupId("customGroupId");
        dependencyElement.setArtifactId("customArtifactId");
        dependencyElement.setType("jar");

        org.codehaus.cargo.maven3.configuration.Container containerElement =
            setUpContainerElement(new EmbeddedLocalContainerStub());
        containerElement.setDependencies(new Dependency[] {dependencyElement});

        org.codehaus.cargo.container.EmbeddedLocalContainer container =
            (EmbeddedLocalContainer) containerElement.createContainer(
                new StandaloneLocalConfigurationStub("configuration/home"), new NullLogger(),
                    createTestCargoProject("whatever", artifacts));

        // 4) Verify that we can load data from our JAR file when using the classloader from the
        // container and when using the context class loader as we set it to be the embedded
        // container classloader.
        assertEquals(resourceValue, getResource(container.getClassLoader(), resourceName));
        assertEquals(resourceValue, getResource(Thread.currentThread().getContextClassLoader(),
            resourceName));
    }

    /**
     * Test embedded container creation with extra classpath location.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateEmbeddedContainerWithExtraClasspathLocation() throws Exception
    {
        // Create a jar file
        String resourceValue = "file in extra classpath";
        File resourceFile = File.createTempFile("maven3-plugin-test-embedded-extra", ".txt");
        resourceFile.deleteOnExit();
        String resourceName = resourceFile.getName();
        String resourceLocation = resourceFile.getParent();

        try (FileOutputStream os = new FileOutputStream(resourceFile))
        {
            os.write(resourceValue.getBytes(StandardCharsets.UTF_8));
        }

        Dependency dependencyElement = new Dependency();
        dependencyElement.setLocation(resourceLocation);

        org.codehaus.cargo.maven3.configuration.Container containerElement =
            setUpContainerElement(new EmbeddedLocalContainerStub());
        containerElement.setDependencies(new Dependency[] {dependencyElement});

        org.codehaus.cargo.container.EmbeddedLocalContainer container =
            (EmbeddedLocalContainer) containerElement.createContainer(
                new StandaloneLocalConfigurationStub("configuration/home"), new NullLogger(),
                    createTestCargoProject("whatever"));

        assertEquals(resourceValue, getResource(container.getClassLoader(), resourceName));
        assertEquals(resourceValue, getResource(Thread.currentThread().getContextClassLoader(),
            resourceName));
    }

    /**
     * Test installed local container creation with installer and home.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateInstalledLocalContainerWithInstallerAndHome() throws Exception
    {
        final String containerHome = "container/overriding_home";
        ZipURLInstaller zipURLInstaller = Mockito.mock(ZipURLInstaller.class);
        Mockito.when(zipURLInstaller.getHome()).thenReturn(containerHome);
        Mockito.when(zipURLInstaller.getDownloadFile()).thenReturn("tmp/somedownloadedfile.zip");

        org.codehaus.cargo.maven3.configuration.Container containerElement =
            setUpContainerElement(new InstalledLocalContainerStub());
        containerElement.setHome(containerHome);
        containerElement
            .setZipUrlInstaller(new org.codehaus.cargo.maven3.configuration.ZipUrlInstaller()
            {
                @Override
                public ZipURLInstaller createInstaller(String ignored)
                {
                    return zipURLInstaller;
                }
            });

        org.codehaus.cargo.container.InstalledLocalContainer container =
            (InstalledLocalContainer) containerElement.createContainer(
                new StandaloneLocalConfigurationStub("configuration/home"), new NullLogger(),
                    createTestCargoProject("whatever"));
        assertEquals("Specified home didn't override home defined by installer", containerHome,
            container.getHome());
        Mockito.verify(zipURLInstaller, Mockito.times(1)).install();
    }

    /**
     * Test installed local container creation with home.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateInstalledLocalContainerWithHome() throws Exception
    {
        org.codehaus.cargo.maven3.configuration.Container containerElement =
            setUpContainerElement(new InstalledLocalContainerStub());
        final String containerHome = "container/home";
        containerElement.setHome(containerHome);

        org.codehaus.cargo.container.InstalledLocalContainer container =
            (InstalledLocalContainer) containerElement.createContainer(
                new StandaloneLocalConfigurationStub("configuration/home"), new NullLogger(),
                    createTestCargoProject("whatever"));
        assertEquals("Specified home not used", containerHome, container.getHome());
    }

    /**
     * Test installed local container creation with installer.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateInstalledLocalContainerWithInstaller() throws Exception
    {
        final String containerHome = "container/installer_home";
        ZipURLInstaller zipURLInstaller = Mockito.mock(ZipURLInstaller.class);
        Mockito.when(zipURLInstaller.getHome()).thenReturn(containerHome);
        Mockito.when(zipURLInstaller.getDownloadFile()).thenReturn("tmp/somedownloadedfile.zip");

        org.codehaus.cargo.maven3.configuration.Container containerElement =
            setUpContainerElement(new InstalledLocalContainerStub());
        containerElement.setHome(null);
        containerElement
            .setZipUrlInstaller(new org.codehaus.cargo.maven3.configuration.ZipUrlInstaller()
            {
                @Override
                public ZipURLInstaller createInstaller(String ignored)
                {
                    return zipURLInstaller;
                }
            });

        org.codehaus.cargo.container.InstalledLocalContainer container =
            (InstalledLocalContainer) containerElement.createContainer(
                new StandaloneLocalConfigurationStub("configuration/home"), new NullLogger(),
                    createTestCargoProject("whatever"));
        assertEquals("Home specified by installer not used", containerHome, container.getHome());
        Mockito.verify(zipURLInstaller, Mockito.times(1)).install();
    }

    /**
     * Test installed local container creation with output.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateInstalledLocalContainerWithOutput() throws Exception
    {
        org.codehaus.cargo.maven3.configuration.Container containerElement =
            setUpContainerElement(new InstalledLocalContainerStub());
        final String output = "container/output.log";
        containerElement.setOutput(output);
        containerElement.setAppend(true);

        org.codehaus.cargo.container.InstalledLocalContainer container =
            (InstalledLocalContainer) containerElement.createContainer(
                new StandaloneLocalConfigurationStub("configuration/home"), new NullLogger(),
                    createTestCargoProject("whatever"));
        assertEquals("Container output not set", output, container.getOutput());
        assertTrue("Container output append not set", container.isAppend());
    }

    /**
     * Test whether container dependencies preserve order.
     * @throws Exception If anything goes wrong.
     * */
    public void testExtraClasspathOrdering() throws Exception
    {
        Dependency firstDependencyElement = new Dependency();
        firstDependencyElement.setLocation("firstDependency.jar");
        Dependency secondDependencyElement = new Dependency();
        secondDependencyElement.setLocation("secondDependency.jar");

        org.codehaus.cargo.maven3.configuration.Container containerElement =
            setUpContainerElement(new InstalledLocalContainerStub());
        containerElement.setDependencies(
            new Dependency[] {firstDependencyElement, secondDependencyElement});

        org.codehaus.cargo.container.InstalledLocalContainer container =
            (InstalledLocalContainer) containerElement.createContainer(
                new StandaloneLocalConfigurationStub("configuration/home"),
                    new NullLogger(), createTestCargoProject("whatever"));

        String[] extraClasspath = container.getExtraClasspath();
        assertEquals("Extra classpath elements count", 2, extraClasspath.length);
        assertEquals("firstDependency.jar", extraClasspath[0]);
        assertEquals("secondDependency.jar", extraClasspath[1]);
    }

    /**
     * Setup a container element.
     * @param container Container definition.
     * @return Container element.
     */
    protected org.codehaus.cargo.maven3.configuration.Container setUpContainerElement(
        org.codehaus.cargo.container.Container container)
    {
        org.codehaus.cargo.maven3.configuration.Container containerElement =
            new org.codehaus.cargo.maven3.configuration.Container();
        containerElement.setContainerId(container.getId());
        containerElement.setImplementation(container.getClass().getName());
        containerElement.setHome("container/home");
        containerElement.setType(container.getType());

        return containerElement;
    }

    /**
     * Provide a test {@link CargoProject} in lieu of the one that is normally generated from the
     * {@link org.apache.maven.project.MavenProject} at runtime.
     * @param packaging Packaging.
     * @param artifacts Artifacts.
     * @return {@link CargoProject} with the given <code>packaging</code> and
     * <code>artifacts</code>.
     */
    protected CargoProject createTestCargoProject(String packaging, Set<Artifact> artifacts)
    {
        return new CargoProject(packaging, "projectGroupId", "projectArtifactId",
            "target", "projectFinalName", artifacts, Mockito.mock(Log.class));
    }

    /**
     * Provide a test {@link CargoProject} in lieu of the one that is normally generated from the
     * {@link org.apache.maven.project.MavenProject} at runtime.
     * @param packaging Packaging.
     * @return {@link CargoProject} with the given <code>packaging</code> and no artifacts.
     */
    protected CargoProject createTestCargoProject(String packaging)
    {
        return createTestCargoProject(packaging, new HashSet<Artifact>());
    }

    /**
     * Get the first line of a resource from a specific classloader.
     * @param classLoader {@link ClassLoader} to load from.
     * @param resourceName Resource name.
     * @return Resource from {@link ClassLoader}.
     * @throws IOException If anything goes wrong.
     */
    public String getResource(ClassLoader classLoader, String resourceName) throws IOException
    {
        InputStreamReader reader = new InputStreamReader(
            classLoader.getResourceAsStream(resourceName), StandardCharsets.UTF_8);
        return new BufferedReader(reader).readLine();
    }
}
