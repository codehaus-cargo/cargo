/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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

/**
 * Mock for {@link org.codehaus.cargo.container.InstalledLocalContainer}. We need a static class
 * rather than using a dynamic mock (which we could get using JMock for example) for when we're
 * testing factory classes which create an object out of a class name.
 * 
 * @version $Id$
 */
public class InstalledLocalContainerStub
    extends AbstractLocalContainerStub implements InstalledLocalContainer
{
    private String home;
    private Map<String, String> systemProperties;

    public static final String ID = "myInstalledLocalContainer";
    public static final String NAME = "My Installed Local Container";

    public InstalledLocalContainerStub()
    {
        this(null);
    }

    public InstalledLocalContainerStub(LocalConfiguration configuration)
    {
        super(configuration);
        setId(ID);
        setName(NAME);
    }

    public String getHome()
    {
        return this.home;
    }

    public void setHome(String home)
    {
        this.home = home;
    }

    public ContainerType getType()
    {
        return ContainerType.INSTALLED;
    }

    public String[] getExtraClasspath()
    {
        throw new RuntimeException("Not implemented");
    }

    public String[] getSharedClasspath()
    {
        throw new RuntimeException("Not implemented");
    }

    public Map<String, String> getSystemProperties()
    {
        return this.systemProperties;
    }

    public void setExtraClasspath(String[] classpath)
    {
        throw new RuntimeException("Not implemented");
    }

    public void addExtraClasspath(String classpath)
    {
        throw new RuntimeException("Not implemented");
    }

    public void setSharedClasspath(String[] classpath)
    {
        throw new RuntimeException("Not implemented");
    }

    public void setSystemProperties(Map<String, String> properties)
    {
        this.systemProperties = properties;
    }

    public void addSharedClasspath(String location)
    {
        throw new RuntimeException("Not implemented");
    }
}
