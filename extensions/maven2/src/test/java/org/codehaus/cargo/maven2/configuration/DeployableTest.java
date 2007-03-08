/*
 * ========================================================================
 *
 * Copyright 2005 Vincent Massol.
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

import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.cargo.maven2.util.CargoProject;
import org.codehaus.cargo.maven2.configuration.Deployable;
import org.codehaus.cargo.maven2.configuration.CustomType;
import org.codehaus.cargo.container.deployable.WAR;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class DeployableTest extends MockObjectTestCase
{
    private Mock mockLog;

    protected void setUp()
    {
        this.mockLog = mock(Log.class);
        this.mockLog.stubs().method("debug");
    }

    public void testCreateDeployableWhenOnlyLocationSpecified() throws Exception
    {
        String deployableFile = "testCreateDeployableWhenOnlyLocationSpecified.war";

        Deployable deployableElement = new Deployable();
        deployableElement.setLocation(deployableFile);

        CargoProject project = createDefaultProject("war", new HashSet());
        org.codehaus.cargo.container.deployable.Deployable deployable =
            deployableElement.createDeployable("whateverId", project);

        //  We verify that we've created an auto-deployable
        assertEquals(deployable.getFile(), deployableFile);
        assertEquals(project.getGroupId(), deployableElement.getGroupId());
        assertEquals(project.getArtifactId(), deployableElement.getArtifactId());
        assertEquals(project.getPackaging(), deployableElement.getType());
    }

    public void testCreateDeployableWhenDeployableIsNotADependency() throws Exception
    {
        Deployable deployableElement = createCustomDeployableElement();

        try
        {
            deployableElement.createDeployable("whateverId",
                createDefaultProject("war", new HashSet()));
            fail("An exception should have been thrown");
        }
        catch (MojoExecutionException expected)
        {
            assertEquals("Artifact [customGroupId:customArtifactId:customType] is not a "
                + "dependency of the project.", expected.getMessage());
        }
    }

    public void testCreateCustomDeployable() throws Exception
    {
        // Custom deployable type
        Deployable deployableElement = createCustomDeployableElement();

        String deployableFile = "testCreateCustomDeployable.custom";

        // Matching dependency definition
        Artifact artifact = createCustomArtifact(deployableFile);

        Set artifacts = new HashSet();
        artifacts.add(artifact);

        org.codehaus.cargo.container.deployable.Deployable deployable =
            deployableElement.createDeployable("whateverId",
                createDefaultProject("war", artifacts));

        assertEquals(CustomType.class.getName(), deployable.getClass().getName());
    }

    public void testComputeLocationWhenEjbPackaging() throws Exception
    {
        Deployable deployableElement = new Deployable();
        deployableElement.setGroupId("projectGroupId");
        deployableElement.setArtifactId("projectArtifactId");
        deployableElement.setType("ejb");

        String location = deployableElement.computeLocation(createDefaultProject("ejb", null));
        assertTrue(location.endsWith("projectFinalName.jar"));
    }

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
     * Setting a Null property is the way Maven2 operates when the user specifies an empty property.
     * We need to verify that the Cargo plugin intercepts that and replaces the Null with an
     * empty String.
     */
    public void testSettingANullDeployableProperty()
    {
        Deployable deployableElement = new Deployable();
        WAR war = new WAR("/some/file.war");

        Map properties = new HashMap();
        properties.put("context", null);
        deployableElement.setProperties(properties);

        deployableElement.setPropertiesOnDeployable(war, createDefaultProject("war", null));

        assertEquals("", war.getContext());
    }

    /**
     * When the groupId and artifactId of a defined deployable match the project's groupId and
     * artifactId then the type must also be compatible with the projects' packaging. If not the
     * deployable will be looked for in the project's dependencies.
     */
    public void testComputeLocationWhenDeployableTypeMismatchWithProjectType() throws Exception
    {
        Deployable deployableElement = new Deployable();
        deployableElement.setGroupId("projectGroupId");
        deployableElement.setArtifactId("projectArtifactId");
        deployableElement.setType("war");

        // Verify that the log warning has been raised too
        this.mockLog.expects(once()).method("warn").with(eq("The defined deployable has the same "
            + "groupId and artifactId as your project's main artifact but the type is different. "
            + "You've defined a [war] type whereas the project's packaging is [something]. This is "
            + "possibly an error and as a consequence the plugin will try to find this deployable "
            + "in the project's dependencies."));

        try
        {
            deployableElement.computeLocation(createDefaultProject("something", new HashSet()));
            fail("An exception should have been raised");
        }
        catch (MojoExecutionException expected)
        {
            assertEquals("Artifact [projectGroupId:projectArtifactId:war] is not a dependency of "
                + "the project.", expected.getMessage());
        }
    }

    private CargoProject createDefaultProject(String packaging, Set artifacts)
    {
        return new CargoProject(packaging, "projectGroupId", "projectArtifactId",
            "projectBuildDirectory", "projectFinalName", artifacts, (Log) this.mockLog.proxy());
    }

    private Deployable createCustomDeployableElement()
    {
        Deployable deployableElement = new Deployable();
        deployableElement.setGroupId("customGroupId");
        deployableElement.setArtifactId("customArtifactId");
        deployableElement.setType("customType");
        deployableElement.setImplementation(CustomType.class.getName());
        return deployableElement;
    }

    private Artifact createCustomArtifact(String deployableFile)
    {
        Mock mockArtifact = mock(Artifact.class);
        mockArtifact.stubs().method("getGroupId").will(returnValue("customGroupId"));
        mockArtifact.stubs().method("getArtifactId").will(returnValue("customArtifactId"));
        mockArtifact.stubs().method("getType").will(returnValue("jar"));
        mockArtifact.expects(atLeastOnce()).method("getFile").will(returnValue(
            new File(deployableFile)));
        return (Artifact) mockArtifact.proxy();
    }
}
