/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
package org.codehaus.cargo.container.wildfly.internal;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.ScriptingCapableContainer;
import org.codehaus.cargo.container.configuration.script.ScriptCommand;
import org.codehaus.cargo.container.spi.startup.AbstractContainerMonitor;
import org.codehaus.cargo.container.wildfly.internal.configuration.factory.WildFlyCliConfigurationFactory;

/**
 * WildFly monitor checking if CLI API is available.
 */
public class CLIWildFlyMonitor extends AbstractContainerMonitor
{

    /**
     * Constructor.
     * 
     * @param container Container to be monitored.
     */
    public CLIWildFlyMonitor(ScriptingCapableContainer container)
    {
        super(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRunning()
    {
        WildFlyConfiguration configuration = (WildFlyConfiguration) getConfiguration();
        WildFlyCliConfigurationFactory factory = configuration.getConfigurationFactory();
        List<ScriptCommand> configurationScript = new ArrayList<ScriptCommand>();
        configurationScript.add(factory.connectToServerScript());
        try
        {
            ((ScriptingCapableContainer) getContainer()).executeScript(configurationScript);
            return true;
        }
        catch (ContainerException ex)
        {
            return false;
        }
    }

}
