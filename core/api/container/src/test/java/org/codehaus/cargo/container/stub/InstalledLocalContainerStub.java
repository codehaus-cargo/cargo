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
package org.codehaus.cargo.container.stub;

import java.util.Map;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.spi.jvm.JvmLauncherFactory;

/**
 * Mock for {@link InstalledLocalContainer}. We need a static class rather than using a dynamic
 * mock (which we could get using JMock for example) for when we're testing factory classes which
 * create an object out of a class name.
 * 
 */
public class InstalledLocalContainerStub extends AbstractLocalContainerStub
    implements InstalledLocalContainer
{
    /**
     * Dummy id.
     */
    public static final String ID = "myInstalledLocalContainer";

    /**
     * Dummy name.
     */
    public static final String NAME = "My Installed Local Container";

    /**
     * Container home.
     */
    private String home;

    /**
     * System properties.
     */
    private Map<String, String> systemProperties;

    /**
     * Allows creating a container with no configuration for test that do not require a
     * configuration.
     */
    public InstalledLocalContainerStub()
    {
        this(null);
    }

    /**
     * Saves the configuration and sets the id and name. {@inheritDoc}
     * @param configuration Container configuration.
     */
    public InstalledLocalContainerStub(LocalConfiguration configuration)
    {
        super(configuration);
        setId(ID);
        setName(NAME);
    }

    /**
     * {@inheritDoc}
     * @return Container home.
     */
    public String getHome()
    {
        return this.home;
    }

    /**
     * {@inheritDoc}
     * @param home Container home.
     */
    public void setHome(String home)
    {
        this.home = home;
    }

    /**
     * {@inheritDoc}
     * @return {@link ContainerType#INSTALLED}
     */
    public ContainerType getType()
    {
        return ContainerType.INSTALLED;
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritDoc}
     * @return Nothing.
     */
    public String[] getExtraClasspath()
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritDoc}
     * @return Nothing.
     */
    public String[] getSharedClasspath()
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritDoc}
     * @param classpath Ignored.
     */
    public void setExtraClasspath(String[] classpath)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritDoc}
     * @param classpath Ignored.
     */
    public void addExtraClasspath(String classpath)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritDoc}
     * @param classpath Ignored.
     */
    public void setSharedClasspath(String[] classpath)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritDoc}
     * @param location Ignored.
     */
    public void addSharedClasspath(String location)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @return System properties.
     */
    public Map<String, String> getSystemProperties()
    {
        return this.systemProperties;
    }

    /**
     * {@inheritDoc}
     * @param properties System properties to set.
     */
    public void setSystemProperties(Map<String, String> properties)
    {
        this.systemProperties = properties;
    }

    /**
     * {@inheritDoc}
     * @param jvmLauncherFactory JVM launcher factory.
     */
    public void setJvmLauncherFactory(JvmLauncherFactory jvmLauncherFactory)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @return JVM launcher factory.
     */
    public JvmLauncherFactory getJvmLauncherFactory()
    {
        throw new RuntimeException("Not implemented");
    }

}
