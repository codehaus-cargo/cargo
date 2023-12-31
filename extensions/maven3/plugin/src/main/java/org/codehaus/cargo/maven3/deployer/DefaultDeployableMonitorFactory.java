/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.maven3.deployer;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.container.deployer.UrlPathDeployableMonitor;
import org.codehaus.cargo.maven3.configuration.Deployable;

/**
 * Default {@link DeployableMonitorFactory} implementation.
 */
public class DefaultDeployableMonitorFactory implements DeployableMonitorFactory
{

    /**
     * {@inheritDoc}
     */
    @Override
    public DeployableMonitor createDeployableMonitor(Container container, Deployable deployable)
    {
        DeployableMonitor deployableMonitor = null;
        Configuration configuration = retrieveConfiguration(container);

        if (deployable.getPingURL() != null)
        {
            deployableMonitor = createUrlDeployableMonitor(deployable);
        }
        else if (deployable.getPingUrlPath() != null)
        {
            deployableMonitor = createUrlPathDeployableMonitor(configuration, deployable);
        }

        return deployableMonitor;
    }

    /**
     * @param deployable Deployable.
     * @return Initialized URLDeployableMonitor.
     */
    private URLDeployableMonitor createUrlDeployableMonitor(Deployable deployable)
    {
        Long pingTimeout = deployable.getPingTimeout();

        if (pingTimeout == null)
        {
            return new URLDeployableMonitor(deployable.getPingURL());
        }
        else
        {
            return new URLDeployableMonitor(deployable.getPingURL(), pingTimeout);
        }
    }

    /**
     * @param configuration Container configuration.
     * @param deployable Deployable.
     * @return Initialized UrlPathDeployableMonitor.
     */
    private UrlPathDeployableMonitor createUrlPathDeployableMonitor(Configuration configuration,
            Deployable deployable)
    {
        Long pingTimeout = deployable.getPingTimeout();

        if (pingTimeout == null)
        {
            return new UrlPathDeployableMonitor(configuration, deployable.getPingUrlPath());
        }
        else
        {
            return new UrlPathDeployableMonitor(configuration, deployable.getPingUrlPath(),
                    pingTimeout);
        }
    }

    /**
     * @param container Container.
     * @return Container configuration.
     */
    private Configuration retrieveConfiguration(Container container)
    {
        if (container instanceof LocalContainer)
        {
            return ((LocalContainer) container).getConfiguration();
        }
        else
        {
            return ((RemoteContainer) container).getConfiguration();
        }
    }
}
