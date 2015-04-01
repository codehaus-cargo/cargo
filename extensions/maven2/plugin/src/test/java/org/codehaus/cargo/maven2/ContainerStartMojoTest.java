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
package org.codehaus.cargo.maven2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.stub.InstalledLocalContainerStub;
import org.codehaus.cargo.container.stub.StandaloneLocalConfigurationStub;
import org.codehaus.cargo.maven2.configuration.Configuration;
import org.codehaus.cargo.maven2.configuration.Container;
import org.codehaus.cargo.maven2.configuration.Deployable;
import org.codehaus.cargo.maven2.util.CargoProject;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * Unit tests for the {@link ContainerStartMojo} mojo.
 * 
 */
public class ContainerStartMojoTest extends MockObjectTestCase
{
    /**
     * Mojo for testing.
     */
    private TestableContainerStartMojo mojo;

    /**
     * Class to capture the Container instance so that we can perform various asserts on it in the
     * different unit tests.
     */
    public class TestableContainerStartMojo extends ContainerStartMojo
    {
        /**
         * Created container.
         */
        public org.codehaus.cargo.container.Container createdContainer;

        /**
         * {@inheritDoc}.
         * @throws MojoExecutionException If creating the container fails.
         */
        @Override
        protected org.codehaus.cargo.container.Container createContainer()
            throws MojoExecutionException
        {
            this.createdContainer = super.createContainer();
            return this.createdContainer;
        }
    }

    /**
     * Test execute when autodeploy location is overriden.
     * @throws Exception If anything goes wrong.
     */
    public void testExecuteWhenAutoDeployLocationIsOverriden() throws Exception
    {
        String deployableFile = "testExecuteWhenAutoDeployLocationIsOverriden.war";

        Deployable deployableElement = new Deployable();
        deployableElement.setLocation(deployableFile);

        setUpMojo(InstalledLocalContainerStub.class, InstalledLocalContainerStub.ID,
            StandaloneLocalConfigurationStub.class);
        this.mojo.setDeployablesElement(new Deployable[] {deployableElement});

        this.mojo.setCargoProject(createTestCargoProject("war"));
        this.mojo.execute();

        // Look for the auto deployable in the list of deployables to deploy.
        LocalContainer localContainer = (LocalContainer) this.mojo.createdContainer;
        assertEquals(1, localContainer.getConfiguration().getDeployables().size());
        org.codehaus.cargo.container.deployable.Deployable autoDeployable =
            localContainer.getConfiguration()
                .getDeployables().get(0);
        assertEquals(deployableFile, autoDeployable.getFile());
    }

    /**
     * Test execute when autodeploy location is non-J2EE.
     * @throws Exception If anything goes wrong.
     */
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
     * Test two executions in a single project.
     * @throws Exception If anything goes wrong.
     */
    public void testTwoExecutionsInProject() throws Exception
    {
        Map<String, org.codehaus.cargo.container.Container> context =
            new HashMap<String, org.codehaus.cargo.container.Container>();
        setUpMojo(InstalledLocalContainerStub.class, InstalledLocalContainerStub.ID,
            StandaloneLocalConfigurationStub.class);
        this.mojo.setPluginContext(context);
        this.mojo.setCargoProject(createTestCargoProject("pom"));

        this.mojo.getConfigurationElement().setHome("foo");
        this.mojo.execute();

        this.mojo.getConfigurationElement().setHome("bar");
        this.mojo.execute();

        assertEquals(2, context.size());
        Iterator<org.codehaus.cargo.container.Container> iter = context.values().iterator();
        org.codehaus.cargo.container.Container container1 = iter.next();
        org.codehaus.cargo.container.Container container2 = iter.next();
        // can't work out which container is which, so we just check they're different
        assertFalse("containers should be different", container1.equals(container2));
    }

    /**
     * Test two executions with different configurations in a single project.
     * @throws Exception If anything goes wrong.
     */
    public void testTwoExecutionsWithDifferentConfigurationsInProject() throws Exception
    {
        Map<String, org.codehaus.cargo.container.Container> context =
            new HashMap<String, org.codehaus.cargo.container.Container>();
        setUpMojo(InstalledLocalContainerStub.class, InstalledLocalContainerStub.ID,
            StandaloneLocalConfigurationStub.class);
        this.mojo.setPluginContext(context);
        this.mojo.setCargoProject(createTestCargoProject("pom"));
        this.mojo.getConfigurationElement().setProperties(new HashMap<String, String>());

        this.mojo.getConfigurationElement().getProperties().put("foo", "bar");
        this.mojo.execute();
        assertEquals(1, context.size());
        Iterator<org.codehaus.cargo.container.Container> iter = context.values().iterator();
        org.codehaus.cargo.container.Container container = iter.next();
        assertEquals("bar",
            ((LocalContainer) container).getConfiguration().getPropertyValue("foo"));

        this.mojo.getConfigurationElement().getProperties().put("foo", "qux");
        this.mojo.execute();
        assertEquals(1, context.size());
        iter = context.values().iterator();
        container = iter.next();
        assertEquals("qux",
            ((LocalContainer) container).getConfiguration().getPropertyValue("foo"));

    }

    /**
     * Provide a test {@link CargoProject} in lieu of the one that is normally generated from the
     * {@link org.apache.maven.project.MavenProject} at runtime.
     * @param packaging Packaging.
     * @return {@link CargoProject} with the given <code>packaging</code>.
     */
    protected CargoProject createTestCargoProject(String packaging)
    {
        Mock mockLog = mock(Log.class);
        mockLog.stubs().method("debug");

        return new CargoProject(packaging, "projectGroupId", "projectArtifactId",
            "target", "projectFinalName", new HashSet<Artifact>(), (Log) mockLog.proxy());
    }

    /**
     * Set up stubbed container and configuration object in order to prevent real actions to happen
     * (like the container starting, etc). We're only interested in asserting that the objects are
     * created correctly here.
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
        this.mojo.setConfigurationElement(configurationElement);

        this.mojo.setDeployablesElement(new Deployable[0]);
    }
}
