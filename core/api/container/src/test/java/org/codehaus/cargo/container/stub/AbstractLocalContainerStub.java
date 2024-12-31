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
package org.codehaus.cargo.container.stub;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.util.FileHandler;

/**
 * Code common to all local container stubs.
 */
public abstract class AbstractLocalContainerStub extends AbstractContainerStub
    implements LocalContainer
{
    /**
     * Configuration.
     */
    private LocalConfiguration configuration;

    /**
     * The file to which output of the container should be written.
     */
    private String output;

    /**
     * Whether output of the container should be appended to an existing file, or the existing file
     * should be truncated.
     */
    private boolean append;

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
     * {@inheritDoc}
     */
    @Override
    public boolean isAppend()
    {
        return append;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAppend(boolean shouldAppend)
    {
        this.append = shouldAppend;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOutput()
    {
        return output;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOutput(String output)
    {
        this.output = output;
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritDoc}
     * @return Nothing.
     */
    @Override
    public long getTimeout()
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritDoc}
     * @param timeout Ignored.
     */
    @Override
    public void setTimeout(long timeout)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @return The saved container configuration.
     */
    @Override
    public LocalConfiguration getConfiguration()
    {
        return this.configuration;
    }

    /**
     * {@inheritDoc}
     * @param configuration Container configuration to save.
     */
    @Override
    public void setConfiguration(LocalConfiguration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritDoc}
     * @return Nothing.
     */
    @Override
    public FileHandler getFileHandler()
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritDoc}
     * @param handler Ignored.
     */
    @Override
    public void setFileHandler(FileHandler handler)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Voluntarily empty for testing. {@inheritDoc}
     */
    @Override
    public void start()
    {
        // Nothing
    }

    /**
     * Voluntarily empty for testing. {@inheritDoc}
     */
    @Override
    public void stop()
    {
        // Nothing
    }

    /**
     * Voluntarily empty for testing. {@inheritDoc}
     */
    @Override
    public void restart()
    {
        // Nothing
    }
}
