/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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

import junit.framework.TestCase;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Reference;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.stub.InstalledLocalContainerStub;
import org.codehaus.cargo.container.stub.StandaloneLocalConfigurationStub;
import org.codehaus.cargo.container.resin.Resin2xStandaloneLocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;

/**
 * Unit tests for {@link CargoTask}.
 *
 * @version $Id$
 */
public class CargoTaskTest extends TestCase
{
    private CargoTask task;
    private ConfigurationElement configurationElement;

    protected void setUp()
    {
        this.task = new CargoTask();

        this.task.setContainerId(getName());
        this.task.setClass(InstalledLocalContainerStub.class);
        this.configurationElement = this.task.createConfiguration();
        this.configurationElement.setClass(StandaloneLocalConfigurationStub.class);
        this.configurationElement.setType("standalone");
        this.configurationElement.setHome("somewhere");
    }

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

        Deployable deployable = (Deployable) container.getConfiguration().getDeployables().get(0); 
        assertEquals(WAR.class.getName(), deployable.getClass().getName());
        assertEquals("some/war", deployable.getFile());
    }


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
            assertEquals("You must specify an [action] attribute with values [configure], [start] or [stop]",
                expected.getMessage());
        }
    }

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
            assertEquals("Valid actions are: [configure], [start] and [stop]", expected.getMessage());
        }
    }

    public void testExecuteStopOk()
    {
        this.task.setAction("stop");
        this.task.setHome("home");
        this.task.execute();
    }
}
