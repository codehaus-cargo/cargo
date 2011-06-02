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

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.spi.util.ContainerUtils;

/**
 * Start a container using Cargo and wait until user pressed CTRL + C to stop.
 * 
 * @version $Id$
 * @goal run
 * @requiresDependencyResolution test
 */
public class ContainerRunMojo extends AbstractContainerStartMojo
{
    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.maven2.AbstractCargoMojo#doExecute()
     */
    @Override
    public void doExecute() throws MojoExecutionException
    {
        super.doExecute();

        getLog().info("Press Ctrl-C to stop the container...");
        ContainerUtils.waitTillContainerIsStopped(this.localContainer);
    }
}
