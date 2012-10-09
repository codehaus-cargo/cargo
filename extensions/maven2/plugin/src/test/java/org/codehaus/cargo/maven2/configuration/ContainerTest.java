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
package org.codehaus.cargo.maven2.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import org.codehaus.cargo.maven2.util.CargoProject;
import org.codehaus.cargo.util.log.NullLogger;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

/**
 * Unit tests for the {@link org.codehaus.cargo.maven2.configuration.Container} class.
 * 
 * @version $Id$
 */
public class ContainerTest extends MockObjectTestCase
{
    /**
     * Test embedded container creation with system properties.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateEmbeddedContainerWithSystemPropertiesSet() throws Exception
    {
        org.codehaus.cargo.maven2.configuration.Container containerElement =
            setUpContainerElement(new EmbeddedLocalContainerStub());

        Map<String, String> props = new HashMap<String, String>();
        props.put("id1", "value1");
        props.put("id2", "value2");

        containerElement.setSystemProperties(props);
        
        File systemPropertiesFile =
            File.createTempFile(ConfigurationTest.class.getName(), ".properties");
        try
        {
            OutputStream outputStream = new FileOutputStream(systemPropertiesFile);
            try
            {        
                Properties properties = new Properties();
                properties.put("id2", "foobar");
                properties.put("id3", "value3");
                properties.store(outputStream, null);
            }
            finally
            {
                outputStream.close();
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
        org.codehaus.cargo.maven2.configuration.Container containerElement =
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
            OutputStream outputStream = new FileOutputStream(systemPropertiesFile);
            try
            {        
                Properties properties = new Properties();
                properties.put("id2", "foobar");
                properties.put("id3", "value3");
                properties.store(outputStream, null);
            }
            finally
            {
                outputStream.close();
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

        File zipFile = File.createTempFile("maven2-plugin-test-dependency", ".zip");
        zipFile.deleteOnExit();

        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile));
        zip.putNextEntry(new ZipEntry(resourceName));
        zip.write(resourceValue.getBytes("UTF-8"));
        zip.close();

        // 2) Create a Maven2 Artifact linked to the JAR file just created
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

        org.codehaus.cargo.maven2.configuration.Container containerElement =
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
        File resourceFile = File.createTempFile("maven2-plugin-test-embedded-extra", ".txt");
        resourceFile.deleteOnExit();
        String resourceName = resourceFile.getName();
        String resourceLocation = resourceFile.getParent();

        FileOutputStream os = new FileOutputStream(resourceFile);
        os.write(resourceValue.getBytes("UTF-8"));
        os.close();

        Dependency dependencyElement = new Dependency();
        dependencyElement.setLocation(resourceLocation);

        org.codehaus.cargo.maven2.configuration.Container containerElement =
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
        org.codehaus.cargo.maven2.configuration.Container containerElement =
            setUpContainerElement(new InstalledLocalContainerStub());
        final String containerHome = "container/overriding_home";
        containerElement.setHome(containerHome);
        final Mock mockInstaller = mock(ZipURLInstaller.class, new Class[] {URL.class},
            new Object[] {new URL("http://whatever")});
        // install method should be called
        mockInstaller.expects(once()).method("install");
        // home provided by installer should not be used
        mockInstaller.stubs().method("getHome").will(returnValue("container/incorrect_home"));
        mockInstaller.stubs().method("getDownloadFile").
            will(returnValue("tmp/somedownloadedfile.zip"));
        containerElement
            .setZipUrlInstaller(new org.codehaus.cargo.maven2.configuration.ZipUrlInstaller()
            {
                @Override
                public ZipURLInstaller createInstaller(String ignored)
                {
                    return (ZipURLInstaller) mockInstaller.proxy();
                }
            });

        org.codehaus.cargo.container.InstalledLocalContainer container =
            (InstalledLocalContainer) containerElement.createContainer(
                new StandaloneLocalConfigurationStub("configuration/home"), new NullLogger(),
                createTestCargoProject("whatever"));
        assertEquals("Specified home didn't override home defined by installer", containerHome,
            container.getHome());
    }

    /**
     * Test installed local container creation with home.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateInstalledLocalContainerWithHome() throws Exception
    {
        org.codehaus.cargo.maven2.configuration.Container containerElement =
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
        org.codehaus.cargo.maven2.configuration.Container containerElement =
            setUpContainerElement(new InstalledLocalContainerStub());
        containerElement.setHome(null);
        final Mock mockInstaller = mock(ZipURLInstaller.class, new Class[] {URL.class},
            new Object[] {new URL("http://whatever")});
        mockInstaller.expects(once()).method("install");
        final String containerHome = "container/installer_home";
        mockInstaller.stubs().method("getHome").will(returnValue(containerHome));
        mockInstaller.stubs().method("getDownloadFile").
            will(returnValue("tmp/somedownloadedfile.zip"));
        containerElement
            .setZipUrlInstaller(new org.codehaus.cargo.maven2.configuration.ZipUrlInstaller()
            {
                @Override
                public ZipURLInstaller createInstaller(String ignored)
                {
                    return (ZipURLInstaller) mockInstaller.proxy();
                }
            });

        org.codehaus.cargo.container.InstalledLocalContainer container =
            (InstalledLocalContainer) containerElement.createContainer(
                new StandaloneLocalConfigurationStub("configuration/home"), new NullLogger(),
                createTestCargoProject("whatever"));
        assertEquals("Home specified by installer not used", containerHome, container.getHome());
    }

    /**
     * Setup a container element.
     * @param container Container definition.
     * @return Container element.
     */
    protected org.codehaus.cargo.maven2.configuration.Container setUpContainerElement(
        org.codehaus.cargo.container.Container container)
    {
        org.codehaus.cargo.maven2.configuration.Container containerElement =
            new org.codehaus.cargo.maven2.configuration.Container();
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
        Mock mockLog = mock(Log.class);
        mockLog.stubs().method("debug");

        return new CargoProject(packaging, "projectGroupId", "projectArtifactId",
            "target", "projectFinalName", artifacts, (Log) mockLog.proxy());
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
        InputStreamReader reader =
            new InputStreamReader(classLoader.getResourceAsStream(resourceName), "UTF-8");
        return new BufferedReader(reader).readLine();
    }
}
