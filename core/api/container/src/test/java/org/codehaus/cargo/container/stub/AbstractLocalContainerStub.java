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

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.util.FileHandler;

/**
 * Code common to all local container stubs.
 * 
 * @version $Id$
 */
public abstract class AbstractLocalContainerStub
    extends AbstractContainerStub implements LocalContainer
{
    private LocalConfiguration configuration;

    public AbstractLocalContainerStub()
    {
        // Allow creating a container with no configuration for test that do not require a
        // configuration
    }

    public AbstractLocalContainerStub(LocalConfiguration configuration)
    {
        setConfiguration(configuration);
    }

    public long getTimeout()
    {
        throw new RuntimeException("Not implemented");
    }

    public boolean isAppend()
    {
        throw new RuntimeException("Not implemented");
    }

    public void setAppend(boolean shouldAppend)
    {
        throw new RuntimeException("Not implemented");
    }

    public String getOutput()
    {
        throw new RuntimeException("Not implemented");
    }

    public void setOutput(String output)
    {
        throw new RuntimeException("Not implemented");
    }

    public void setTimeout(long timeout)
    {
        throw new RuntimeException("Not implemented");
    }

    public LocalConfiguration getConfiguration()
    {
        return this.configuration;
    }

    public void setConfiguration(LocalConfiguration configuration)
    {
        this.configuration = configuration;
    }

    public void start()
    {
        // Voluntarily empty for testing
    }

    public void stop()
    {
        // Voluntarily empty for testing
    }

    public void setFileHandler(FileHandler handler)
    {
        throw new RuntimeException("Not implemented");
    }

    public FileHandler getFileHandler()
    {
        throw new RuntimeException("Not implemented");
    }
}
