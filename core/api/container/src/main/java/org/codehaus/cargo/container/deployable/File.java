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
package org.codehaus.cargo.container.deployable;

import org.codehaus.cargo.container.spi.deployable.AbstractDeployable;

/**
 * Wraps a file that will be deployed into the container.
 * The file type can be used to deploy an generic file or directory
 * to the containers deploy directory. 
 * 
 * @version $Id$
 *
 */
public class File extends AbstractDeployable
{

    /**
     * Constructor.
     * @param file The file name
     */
    public File(String file)
    {
        super(file);
    }

    /**
     * {@inheritDoc}
     */
    public DeployableType getType()
    {
        return DeployableType.FILE;
    }

}
