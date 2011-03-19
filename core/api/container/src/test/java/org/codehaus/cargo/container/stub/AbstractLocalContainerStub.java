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

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.util.FileHandler;

/**
 * Code common to all local container stubs.
 * 
 * @version $Id$
 */
public abstract class AbstractLocalContainerStub extends AbstractContainerStub
    implements LocalContainer
{
    /**
     * Configuration.
     */
    private LocalConfiguration configuration;

    /**
     * Allows creating a container with no configuration for test that do not require a
     * configuration.
     */
    public AbstractLocalContainerStub()
    {
        // Nothing
    }

    /**
     * Saves the configuration.
     * @param configuration Container configuration.
     */
    public AbstractLocalContainerStub(LocalConfiguration configuration)
    {
        setConfiguration(configuration);
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritdoc}
     * @return Nothing.
     */
    public boolean isAppend()
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritdoc}
     * @param shouldAppend Ignored.
     */
    public void setAppend(boolean shouldAppend)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritdoc}
     * @return Nothing.
     */
    public String getOutput()
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritdoc}
     * @param output Ignored.
     */
    public void setOutput(String output)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritdoc}
     * @return Nothing.
     */
    public long getTimeout()
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritdoc}
     * @param timeout Ignored.
     */
    public void setTimeout(long timeout)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritdoc}
     * @return The saved container configuration.
     */
    public LocalConfiguration getConfiguration()
    {
        return this.configuration;
    }

    /**
     * {@inheritdoc}
     * @param configuration Container configuration to save.
     */
    public void setConfiguration(LocalConfiguration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritdoc}
     * @return Nothing.
     */
    public FileHandler getFileHandler()
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritdoc}
     * @param handler Ignored.
     */
    public void setFileHandler(FileHandler handler)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Voluntarily empty for testing. {@inheritdoc}
     */
    public void start()
    {
        // Nothing
    }

    /**
     * Voluntarily empty for testing. {@inheritdoc}
     */
    public void stop()
    {
        // Nothing
    }
}
