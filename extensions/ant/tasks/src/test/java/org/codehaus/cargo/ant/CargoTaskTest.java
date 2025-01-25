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
package org.codehaus.cargo.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Reference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.resin.Resin3xStandaloneLocalConfiguration;
import org.codehaus.cargo.container.stub.InstalledLocalContainerStub;
import org.codehaus.cargo.container.stub.StandaloneLocalConfigurationStub;

/**
 * Unit tests for {@link CargoTask}.
 */
public class CargoTaskTest
{
    /**
     * Cargo Ant task.
     */
    private CargoTask task;

    /**
     * Cargo Ant task configuration element.
     */
    private ConfigurationElement configurationElement;

    /**
     * Creates the various Ant task attributes.
     * @throws Exception If anything goes wrong.
     */
    @BeforeEach
    protected void setUp() throws Exception
    {
        Project antProject = new Project();
        antProject.init();

        this.task = new CargoTask();

        this.task.setProject(antProject);
        this.task.setContainerId("dummy-containerid");
        this.task.setClass(InstalledLocalContainerStub.class);
        this.configurationElement = this.task.createConfiguration();
        this.configurationElement.setClass(StandaloneLocalConfigurationStub.class);
        this.configurationElement.setType("standalone");
        this.configurationElement.setHome("somewhere");
    }

    /**
     * Test the creation of a container with one deployable.
     */
    @Test
    public void testMakeContainerWithOneDeployable()
    {
        CargoTask task = new CargoTask();
        task.setProject(this.task.getProject());
        task.setContainerId("resin3x");
        task.setType(ContainerType.INSTALLED);

        ConfigurationElement configurationElement = task.createConfiguration();
        configurationElement.setType("standalone");
        DeployableElement warElement = new DeployableElement();
        warElement.setType(DeployableType.WAR.getType());
        warElement.setFile("some/war");
        configurationElement.addConfiguredDeployable(warElement);
        configurationElement.setHome("somewhere");

        LocalContainer container = (LocalContainer) task.makeContainer();

        Assertions.assertEquals(Resin3xStandaloneLocalConfiguration.class.getName(),
            container.getConfiguration().getClass().getName());
        Assertions.assertEquals(1, container.getConfiguration().getDeployables().size());

        Deployable deployable = container.getConfiguration().getDeployables().get(0);
        Assertions.assertEquals(WAR.class.getName(), deployable.getClass().getName());
        Assertions.assertEquals("some/war", deployable.getFile());
    }

    /**
     * Test execution with a valid <code>RefId</code>.
     */
    @Test
    public void testExecuteWhenUsingValidRefId()
    {
        this.task.setId("testRefId");
        this.task.setHome("home");
        this.task.execute();

        CargoTask task2 = new CargoTask();
        task2.setProject(this.task.getProject());
        task2.setRefId(new Reference("testRefId"));
        task2.setAction("start");
        task2.execute();
    }

    /**
     * Test execution with an invalid <code>RefId</code>.
     */
    @Test
    public void testExecuteWhenUsingInvalidRefId()
    {
        this.task.setRefId(new Reference("someInexistentReference"));

        try
        {
            this.task.execute();
            Assertions.fail("Should have thrown an exception for a non-inexistent reference here");
        }
        catch (BuildException expected)
        {
            Assertions.assertEquals(
                "The [someInexistentReference] reference does not exist. You must first "
                    + "define a Cargo container reference.", expected.getMessage());
        }
    }

    /**
     * Test execution with no action.
     */
    @Test
    public void testExecuteWithNoAction()
    {
        try
        {
            this.task.execute();
            Assertions.fail(
                "Should have thrown an exception because an action must be specified unless the "
                    + "id attribute is set");
        }
        catch (BuildException expected)
        {
            final String messageStart = "You must specify an [action] attribute with values ";
            Assertions.assertTrue(expected.getMessage().startsWith(messageStart),
                expected.getMessage() + " does not start with: " + messageStart);
        }
    }

    /**
     * Test execution with invalid action.
     */
    @Test
    public void testExecuteWithInvalidAction()
    {
        try
        {
            this.task.setAction("invalidAction");
            this.task.execute();
            Assertions.fail("Should have thrown an exception for invalid action");
        }
        catch (BuildException expected)
        {
            final String messageStart = "Unknown action: ";
            Assertions.assertTrue(expected.getMessage().startsWith(messageStart),
                expected.getMessage() + " does not start with: " + messageStart);
        }
    }

    /**
     * Test execution with action <code>stop</code>.
     */
    @Test
    public void testExecuteStopOk()
    {
        this.task.setAction("stop");
        this.task.setHome("home");
        this.task.execute();
    }

    /**
     * Test the replacement with absolute directories.
     */
    @Test
    public void testAbsoluteDirectoryReplacement()
    {
        this.task.setAction("stop");
        this.task.setHome("home");
        this.task.createConfiguration();
        this.task.getConfiguration().setHome("configuration-home");
        this.task.createZipURLInstaller().setDownloadDir("downlad-dir");
        this.task.createZipURLInstaller().setExtractDir("extract-dir");

        Assertions.assertFalse(new File(this.task.getHome()).isAbsolute(),
            "Container home is already absolute");
        Assertions.assertFalse(new File(this.task.getConfiguration().getHome()).isAbsolute(),
            "Container configuration home is already absolute");
        Assertions.assertFalse(
            new File(this.task.getZipURLInstaller().getDownloadDir()).isAbsolute(),
                "Zip URL installer download directory is already absolute");
        Assertions.assertFalse(
            new File(this.task.getZipURLInstaller().getExtractDir()).isAbsolute(),
                "Zip URL installer extract directory is already absolute");

        this.task.execute();

        Assertions.assertTrue(new File(this.task.getHome()).isAbsolute(),
            "Container home is not absolute");
        Assertions.assertTrue(new File(this.task.getConfiguration().getHome()).isAbsolute(),
            "Container configuration home is not absolute");
        Assertions.assertTrue(
            new File(this.task.getZipURLInstaller().getDownloadDir()).isAbsolute(),
                "Zip URL installer download directory is not absolute");
        Assertions.assertTrue(
            new File(this.task.getZipURLInstaller().getExtractDir()).isAbsolute(),
                "Zip URL installer extract directory is not absolute");
    }
}
