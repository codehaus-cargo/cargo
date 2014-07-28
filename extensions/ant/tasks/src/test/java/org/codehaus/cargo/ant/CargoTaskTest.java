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
package org.codehaus.cargo.ant;

import java.io.File;

import junit.framework.TestCase;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Reference;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.resin.Resin2xStandaloneLocalConfiguration;
import org.codehaus.cargo.container.stub.InstalledLocalContainerStub;
import org.codehaus.cargo.container.stub.StandaloneLocalConfigurationStub;

/**
 * Unit tests for {@link CargoTask}.
 * 
 * @version $Id$
 */
public class CargoTaskTest extends TestCase
{
    /**
     * Cargo ANT task.
     */
    private CargoTask task;

    /**
     * Cargo ANT task configuration element.
     */
    private ConfigurationElement configurationElement;

    /**
     * Creates the various ANT task attributes. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.task = new CargoTask();

        this.task.setContainerId(getName());
        this.task.setClass(InstalledLocalContainerStub.class);
        this.configurationElement = this.task.createConfiguration();
        this.configurationElement.setClass(StandaloneLocalConfigurationStub.class);
        this.configurationElement.setType("standalone");
        this.configurationElement.setHome("somewhere");
    }

    /**
     * Test the creation of a container with one deployable.
     */
    public void testMakeContainerWithOneDeployable()
    {
        CargoTask task = new CargoTask();
        task.setContainerId("resin2x");
        task.setType(ContainerType.INSTALLED);

        ConfigurationElement configurationElement = task.createConfiguration();
        configurationElement.setType("standalone");
        DeployableElement warElement = new DeployableElement();
        warElement.setType(DeployableType.WAR.getType());
        warElement.setFile("some/war");
        configurationElement.addConfiguredDeployable(warElement);
        configurationElement.setHome("somewhere");

        LocalContainer container = (LocalContainer) task.makeContainer();

        assertEquals(Resin2xStandaloneLocalConfiguration.class.getName(),
            container.getConfiguration().getClass().getName());
        assertEquals(1, container.getConfiguration().getDeployables().size());

        Deployable deployable = container.getConfiguration().getDeployables().get(0);
        assertEquals(WAR.class.getName(), deployable.getClass().getName());
        assertEquals("some/war", deployable.getFile());
    }

    /**
     * Test execution with a valid <code>RefId</code>.
     */
    public void testExecuteWhenUsingValidRefId()
    {
        Project antProject = new Project();
        antProject.init();

        this.task.setProject(antProject);
        this.task.setId("testRefId");
        this.task.setHome("home");
        this.task.execute();

        CargoTask task2 = new CargoTask();
        task2.setProject(antProject);
        task2.setRefId(new Reference("testRefId"));
        task2.setAction("start");
        task2.execute();
    }

    /**
     * Test execution with an invalid <code>RefId</code>.
     */
    public void testExecuteWhenUsingInvalidRefId()
    {
        Project antProject = new Project();
        antProject.init();

        this.task.setProject(antProject);
        this.task.setRefId(new Reference("someInexistentReference"));

        try
        {
            this.task.execute();
            fail("Should have thrown an exception for a non-inexistent reference here");
        }
        catch (BuildException expected)
        {
            assertEquals("The [someInexistentReference] reference does not exist. You must first "
                + "define a Cargo container reference.", expected.getMessage());
        }
    }

    /**
     * Test execution with no action.
     */
    public void testExecuteWithNoAction()
    {
        try
        {
            this.task.execute();
            fail("Should have thrown an exception because an action must be specified unless the "
                + "id attribute is set");
        }
        catch (BuildException expected)
        {
            final String messageStart = "You must specify an [action] attribute with values ";
            assertTrue(expected.getMessage() + " does not start with: " + messageStart,
                expected.getMessage().startsWith(messageStart));
        }
    }

    /**
     * Test execution with invalid action.
     */
    public void testExecuteWithInvalidAction()
    {
        try
        {
            this.task.setAction("invalidAction");
            this.task.execute();
            fail("Should have thrown an exception for invalid action");
        }
        catch (BuildException expected)
        {
            final String messageStart = "Unknown action: ";
            assertTrue(expected.getMessage() + " does not start with: " + messageStart,
                expected.getMessage().startsWith(messageStart));
        }
    }

    /**
     * Test execution with action <code>stop</code>.
     */
    public void testExecuteStopOk()
    {
        this.task.setAction("stop");
        this.task.setHome("home");
        this.task.execute();
    }

    /**
     * Test the replacement with absolute directories.
     */
    public void testAbsoluteDirectoryReplacement()
    {
        this.task.setAction("stop");
        this.task.setHome("home");
        this.task.createConfiguration();
        this.task.getConfiguration().setHome("configuration-home");
        this.task.createZipURLInstaller().setDownloadDir("downlad-dir");
        this.task.createZipURLInstaller().setExtractDir("extract-dir");

        assertFalse("Container home is already absolute",
            new File(this.task.getHome()).isAbsolute());
        assertFalse("Container configuration home is already absolute",
            new File(this.task.getConfiguration().getHome()).isAbsolute());
        assertFalse("Zip URL installer download directory is already absolute",
            new File(this.task.getZipURLInstaller().getDownloadDir()).isAbsolute());
        assertFalse("Zip URL installer extract directory is already absolute",
            new File(this.task.getZipURLInstaller().getExtractDir()).isAbsolute());

        this.task.execute();

        assertTrue("Container home is not absolute",
            new File(this.task.getHome()).isAbsolute());
        assertTrue("Container configuration home is not absolute",
            new File(this.task.getConfiguration().getHome()).isAbsolute());
        assertTrue("Zip URL installer download directory is not absolute",
            new File(this.task.getZipURLInstaller().getDownloadDir()).isAbsolute());
        assertTrue("Zip URL installer extract directory is not absolute",
            new File(this.task.getZipURLInstaller().getExtractDir()).isAbsolute());
    }
}
