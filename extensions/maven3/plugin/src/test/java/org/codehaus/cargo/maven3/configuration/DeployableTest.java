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
package org.codehaus.cargo.maven3.configuration;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.maven3.util.CargoProject;
import org.mockito.Mockito;

/**
 * Unit tests for the {@link Deployable} class.
 */
public class DeployableTest extends TestCase
{
    /**
     * Mock {@link Log} implementation.
     */
    private Log mockLog;

    /**
     * {@inheritDoc}. Mock {@link Log} implementation.
     */
    @Override
    protected void setUp()
    {
        this.mockLog = Mockito.mock(Log.class);
    }

    /**
     * Test create deployable when only its location is specified.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateDeployableWhenOnlyLocationSpecified() throws Exception
    {
        String deployableFile = "testCreateDeployableWhenOnlyLocationSpecified.war";

        Deployable deployableElement = new Deployable();
        deployableElement.setLocation(deployableFile);

        CargoProject project = createDefaultProject("war", new HashSet<Artifact>());
        org.codehaus.cargo.container.deployable.Deployable deployable =
            deployableElement.createDeployable("whateverId", project);

        // We verify that we've created an auto-deployable
        assertEquals(deployable.getFile(), deployableFile);
        assertEquals(project.getGroupId(), deployableElement.getGroupId());
        assertEquals(project.getArtifactId(), deployableElement.getArtifactId());
        assertEquals(project.getPackaging(), deployableElement.getType());
    }

    /**
     * Test create deployable when it is not a dependency.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateDeployableWhenDeployableIsNotADependency() throws Exception
    {
        Deployable deployableElement = createCustomDeployableElement();

        try
        {
            deployableElement.createDeployable("whateverId",
                createDefaultProject("war", new HashSet<Artifact>()));
            fail("An exception should have been thrown");
        }
        catch (MojoExecutionException expected)
        {
            assertEquals("Artifact [customGroupId:customArtifactId:customType] is not a "
                + "dependency of the project.", expected.getMessage());
        }
    }

    /**
     * Test custom deployable creation.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateCustomDeployable() throws Exception
    {
        // Custom deployable type
        Deployable deployableElement = createCustomDeployableElement();

        String deployableFile = "testCreateCustomDeployable.custom";

        // Matching dependency definition
        Artifact artifact = createCustomArtifact(deployableFile);

        Set<Artifact> artifacts = new HashSet<Artifact>();
        artifacts.add(artifact);

        org.codehaus.cargo.container.deployable.Deployable deployable =
            deployableElement.createDeployable("whateverId",
                createDefaultProject("war", artifacts));

        assertEquals(CustomType.class.getName(), deployable.getClass().getName());
    }

    /**
     * Test compute location for EJBs.
     * @throws Exception If anything goes wrong.
     */
    public void testComputeLocationWhenEjbPackaging() throws Exception
    {
        Deployable deployableElement = new Deployable();
        deployableElement.setGroupId("projectGroupId");
        deployableElement.setArtifactId("projectArtifactId");
        deployableElement.setType("ejb");

        String location = deployableElement.computeLocation(createDefaultProject("ejb", null));
        assertTrue(location.endsWith("projectFinalName.jar"));
    }

    /**
     * Test compute location for uberwars.
     * @throws Exception If anything goes wrong.
     */
    public void testComputeLocationWhenUberwarPackaging() throws Exception
    {
        Deployable deployableElement = new Deployable();
        deployableElement.setGroupId("projectGroupId");
        deployableElement.setArtifactId("projectArtifactId");
        deployableElement.setType("war");

        String location = deployableElement.computeLocation(createDefaultProject("uberwar", null));
        assertTrue(location.endsWith("projectFinalName.war"));
    }

    /**
     * Test compute location for JBoss SARs.
     * @throws Exception If anything goes wrong.
     */
    public void testComputeLocationWhenJBossSarPackaging() throws Exception
    {
        Deployable deployableElement = new Deployable();
        deployableElement.setGroupId("projectGroupId");
        deployableElement.setArtifactId("projectArtifactId");
        deployableElement.setType("sar");

        String location = deployableElement
            .computeLocation(createDefaultProject("jboss-sar", null));
        assertTrue(location, location.endsWith("projectFinalName.sar"));

        // Verify that the log warning has not been raised
        Mockito.verify(this.mockLog, Mockito.times(0)).warn((CharSequence) Mockito.any());
        Mockito.verify(this.mockLog, Mockito.times(0)).warn((Throwable) Mockito.any());
        Mockito.verify(this.mockLog, Mockito.times(0)).warn(Mockito.any(), Mockito.any());
    }

    /**
     * Test compute location for JBoss HARs.
     * @throws Exception If anything goes wrong.
     */
    public void testComputeLocationWhenJBossHarPackaging() throws Exception
    {
        Deployable deployableElement = new Deployable();
        deployableElement.setGroupId("projectGroupId");
        deployableElement.setArtifactId("projectArtifactId");
        deployableElement.setType("har");

        String location = deployableElement
            .computeLocation(createDefaultProject("jboss-har", null));
        assertTrue(location, location.endsWith("projectFinalName.har"));

        // Verify that the log warning has not been raised
        Mockito.verify(this.mockLog, Mockito.times(0)).warn((CharSequence) Mockito.any());
        Mockito.verify(this.mockLog, Mockito.times(0)).warn((Throwable) Mockito.any());
        Mockito.verify(this.mockLog, Mockito.times(0)).warn(Mockito.any(), Mockito.any());
    }

    /**
     * Test compute location for JBoss Spring packages.
     * @throws Exception If anything goes wrong.
     */
    public void testComputeLocationWhenJBossSpringPackaging() throws Exception
    {
        Deployable deployableElement = new Deployable();
        deployableElement.setGroupId("projectGroupId");
        deployableElement.setArtifactId("projectArtifactId");
        deployableElement.setType("spring");

        String location = deployableElement.computeLocation(createDefaultProject("jboss-spring",
            null));
        assertTrue(location, location.endsWith("projectFinalName.spring"));

        // Verify that the log warning has not been raised
        Mockito.verify(this.mockLog, Mockito.times(0)).warn((CharSequence) Mockito.any());
        Mockito.verify(this.mockLog, Mockito.times(0)).warn((Throwable) Mockito.any());
        Mockito.verify(this.mockLog, Mockito.times(0)).warn(Mockito.any(), Mockito.any());
    }

    /**
     * Test compute location for JBoss ESB packagings.
     * @throws Exception If anything goes wrong.
     */
    public void testComputeLocationWhenJBossEsbPackaging() throws Exception
    {
        Deployable deployableElement = new Deployable();
        deployableElement.setGroupId("projectGroupId");
        deployableElement.setArtifactId("projectArtifactId");
        deployableElement.setType("esb");

        String location = deployableElement
            .computeLocation(createDefaultProject("jboss-esb", null));
        assertTrue(location, location.endsWith("projectFinalName.esb"));

        // Verify that the log warning has not been raised
        Mockito.verify(this.mockLog, Mockito.times(0)).warn((CharSequence) Mockito.any());
        Mockito.verify(this.mockLog, Mockito.times(0)).warn((Throwable) Mockito.any());
        Mockito.verify(this.mockLog, Mockito.times(0)).warn(Mockito.any(), Mockito.any());
    }

    /**
     * Test compute location for file types.
     * @throws Exception If anything goes wrong.
     */
    public void testComputeLocationWhenAnyPackagingWithDeployableTypeFile() throws Exception
    {
        Deployable deployableElement = new Deployable();
        deployableElement.setGroupId("projectGroupId");
        deployableElement.setArtifactId("projectArtifactId");
        deployableElement.setType("file");

        String location = deployableElement.computeLocation(createDefaultProject(
            "somerandompackaging", null));
        assertTrue(location, location.endsWith("projectFinalName.somerandompackaging"));

        // Verify that the log warning has not been raised
        Mockito.verify(this.mockLog, Mockito.times(0)).warn((CharSequence) Mockito.any());
        Mockito.verify(this.mockLog, Mockito.times(0)).warn((Throwable) Mockito.any());
        Mockito.verify(this.mockLog, Mockito.times(0)).warn(Mockito.any(), Mockito.any());
    }

    /**
     * Setting a Null property is the way Maven 3 operates when the user specifies an empty
     * property. We need to verify that the Cargo plugin intercepts that and replaces the Null with
     * an empty String.
     * @throws Exception If anything goes wrong.
     */
    public void testSettingANullDeployableProperty() throws Exception
    {
        Deployable deployableElement = new Deployable();
        WAR war = new WAR("/some/file.war");

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("context", null);
        deployableElement.setProperties(properties);

        deployableElement.setPropertiesOnDeployable(war, createDefaultProject("war", null));

        assertEquals("", war.getContext());
    }

    /**
     * When the groupId and artifactId of a defined deployable match the project's groupId and
     * artifactId then the type must also be compatible with the projects' packaging. If not the
     * deployable will be looked for in the project's dependencies.
     * @throws Exception If anything goes wrong.
     */
    public void testComputeLocationWhenDeployableTypeMismatchWithProjectType() throws Exception
    {
        Deployable deployableElement = new Deployable();
        deployableElement.setGroupId("projectGroupId");
        deployableElement.setArtifactId("projectArtifactId");
        deployableElement.setType("war");

        try
        {
            deployableElement.computeLocation(createDefaultProject("something",
                new HashSet<Artifact>()));
            fail("An exception should have been raised");
        }
        catch (MojoExecutionException expected)
        {
            assertEquals("Artifact [projectGroupId:projectArtifactId:war] is not a dependency of "
                + "the project.", expected.getMessage());

            // Verify that the log warning has been raised too
            Mockito.verify(this.mockLog, Mockito.times(1)).warn(
                "The defined deployable has the same groupId and artifactId as your project's "
                + "main artifact but the type is different. You've defined a [war] type "
                + "whereas the project's packaging is [something]. This is possibly an error "
                + "and as a consequence the plugin will try to find this deployable in the "
                + "project's dependencies.");
        }
    }

    /**
     * Provide a test {@link CargoProject} in lieu of the one that is normally generated from the
     * {@link org.apache.maven.project.MavenProject} at runtime.
     * @param packaging Packaging.
     * @param artifacts Artifacts.
     * @return {@link CargoProject} with the given <code>packaging</code> and
     * <code>artifacts</code>.
     */
    private CargoProject createDefaultProject(String packaging, Set<Artifact> artifacts)
    {
        return new CargoProject(packaging, "projectGroupId", "projectArtifactId",
            "projectBuildDirectory", "projectFinalName",
            artifacts == null ? Collections.<Artifact>emptySet() : artifacts,
            this.mockLog);
    }

    /**
     * @return A custom {@link Deployable} element.
     */
    private Deployable createCustomDeployableElement()
    {
        Deployable deployableElement = new Deployable();
        deployableElement.setGroupId("customGroupId");
        deployableElement.setArtifactId("customArtifactId");
        deployableElement.setType("customType");
        deployableElement.setImplementation(CustomType.class.getName());
        return deployableElement;
    }

    /**
     * Create a custom artifact.
     * @param deployableFile Deployable file name.
     * @return {@link Artifact} for given <code>deployableFile</code>.
     */
    private Artifact createCustomArtifact(String deployableFile)
    {
        Artifact mockArtifact = Mockito.mock(Artifact.class);
        Mockito.when(mockArtifact.getGroupId()).thenReturn("customGroupId");
        Mockito.when(mockArtifact.getArtifactId()).thenReturn("customArtifactId");
        Mockito.when(mockArtifact.getType()).thenReturn("customType");
        Mockito.when(mockArtifact.getClassifier()).thenReturn(null);
        Mockito.when(mockArtifact.getFile()).thenReturn(new File(deployableFile));
        return mockArtifact;
    }
}
