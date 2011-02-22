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

import java.util.List;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.FileConfig;
import org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration;

/**
 * Mock for {@link LocalConfiguration}. We need a static mock rather than a dynamic mock (which we
 * could get using JMock for example) because we're testing factory classes which create an object
 * out of a class name.
 * 
 * @version $Id$
 */
public class StandaloneLocalConfigurationStub extends AbstractLocalConfigurationStub
    implements StandaloneLocalConfiguration
{
    /**
     * {@inheritdoc}
     * @param home Configuration home.
     */
    public StandaloneLocalConfigurationStub(String home)
    {
        super(home);
    }

    /**
     * {@inheritdoc}
     * @return {@link ConfigurationType#STANDALONE}
     */
    public ConfigurationType getType()
    {
        return ConfigurationType.STANDALONE;
    }

    /**
     * {@inheritdoc}
     * @return <code>null</code>
     */
    public List<FileConfig> getFileProperties()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Doesn't do anything. {@inheritdoc}
     * @param fileConfig Ignored.
     */
    public void setConfigFileProperty(FileConfig fileConfig)
    {
        // TODO Auto-generated method stub
    }

    /**
     * Doesn't do anything. {@inheritdoc}
     * @param fileConfig Ignored.
     */
    public void setFileProperty(FileConfig fileConfig)
    {
        // TODO Auto-generate method stub
    }

    /**
     * {@inheritdoc}
     * @return <code>null</code>
     */
    public FilterChain getFilterChain()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
