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

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.spi.util.ContainerUtils;

/**
 * Configure a container using Cargo.
 * 
 * @version $Id$
 * @goal configure
 * @requiresDependencyResolution compile
 */
public class ContainerConfigureMojo extends AbstractCargoMojo
{
    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.maven2.AbstractCargoMojo#doExecute()
     */
    public void doExecute() throws MojoExecutionException
    {
        Container container = createContainer();

        if (!container.getType().isLocal())
        {
            throw new MojoExecutionException("Only local containers can be configured");
        }

        LocalContainer localContainer = (LocalContainer) container;
        localContainer.getConfiguration().configure(localContainer);
    }

}
