/*
 * ========================================================================
 *
 * Copyright 2005-2006 Vincent Massol.
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

import java.util.HashSet;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.stub.InstalledLocalContainerStub;
import org.codehaus.cargo.container.stub.StandaloneLocalConfigurationStub;
import org.codehaus.cargo.maven2.util.CargoProject;
import org.codehaus.cargo.maven2.configuration.Container;
import org.codehaus.cargo.maven2.configuration.Deployable;
import org.codehaus.cargo.maven2.configuration.Configuration;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class ContainerStartMojoTest extends MockObjectTestCase
{
    private TestableContainerStartMojo mojo;

    // Class to capture the Container instance so that we can perform various asserts on it in
    // the different unit tests
    public class TestableContainerStartMojo extends ContainerStartMojo
    {
        public org.codehaus.cargo.container.Container createdContainer;

        protected org.codehaus.cargo.container.Container createContainer()
            throws MojoExecutionException
        {
            this.createdContainer = super.createContainer();
            return this.createdContainer;
        }
    }

    public void testExecuteWhenAutoDeployLocationIsOverriden() throws Exception
    {
        String deployableFile = "testExecuteWhenAutoDeployLocationIsOverriden.war";

        Deployable deployableElement = new Deployable();
        deployableElement.setLocation(deployableFile);

        setUpMojo(InstalledLocalContainerStub.class, InstalledLocalContainerStub.ID,
            StandaloneLocalConfigurationStub.class);
        this.mojo.getConfigurationElement().setDeployables(new Deployable[] {deployableElement});

        this.mojo.setCargoProject(createTestCargoProject("war"));
        this.mojo.execute();

        // Look for the auto deployable in the list of deployables to deploy.
        LocalContainer localContainer = (LocalContainer) this.mojo.createdContainer;
        assertEquals(1, localContainer.getConfiguration().getDeployables().size());
        org.codehaus.cargo.container.deployable.Deployable autoDeployable =
            (org.codehaus.cargo.container.deployable.Deployable) localContainer.getConfiguration()
                .getDeployables().get(0);
        assertEquals(deployableFile, autoDeployable.getFile());
    }

    public void testExecuteWhenNoAutoDeployableBecauseNonJ2EEPackagingProject() throws Exception
    {
        setUpMojo(InstalledLocalContainerStub.class, InstalledLocalContainerStub.ID,
            StandaloneLocalConfigurationStub.class);
        this.mojo.setCargoProject(createTestCargoProject("whatever"));
        this.mojo.execute();
        LocalContainer localContainer = (LocalContainer) this.mojo.createdContainer;
        assertEquals(0, localContainer.getConfiguration().getDeployables().size());
    }

    /**
     * Provide a test CargoProject in lieu of the one that is normally generated from the
     * MavenProject at runtime.
     */
    protected CargoProject createTestCargoProject(String packaging)
    {
        Mock mockLog = mock(Log.class);
        mockLog.stubs().method("debug");

        return new CargoProject(packaging, "projectGroupId", "projectArtifactId",
            "target", "projectFinalName", new HashSet(), (Log) mockLog.proxy());
    }

    /**
     * Set up stubbed container and configuration object in order to prevent real actions to happen
     * (like the container starting, etc). We're only interested in asserting that the objects
     * are created correctly here.
     *
     * @param containerStubClass the stubbed container class to use
     * @param containerId the container id for the stubbed container
     * @param configurationStubClass the stubbed configuration class to use
     */
    protected void setUpMojo(Class containerStubClass, String containerId,
        Class configurationStubClass)
    {
        this.mojo = new TestableContainerStartMojo();

        Container containerElement = new Container();
        containerElement.setImplementation(containerStubClass.getName());
        containerElement.setContainerId(containerId);
        containerElement.setHome("container/home");
        this.mojo.setContainerElement(containerElement);

        Configuration configurationElement = new Configuration();
        configurationElement.setImplementation(configurationStubClass.getName());
        configurationElement.setHome("configuration/home");
        configurationElement.setDeployables(new Deployable[0]);
        this.mojo.setConfigurationElement(configurationElement);
    }
}
