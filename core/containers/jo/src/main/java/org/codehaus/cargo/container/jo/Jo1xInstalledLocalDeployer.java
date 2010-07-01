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
package org.codehaus.cargo.container.jo;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;

/**
 * Static deployer that deploys WARs to the jo! <code>webapp/host</code> directory.
 *  
 * @version $Id$
 */
public class Jo1xInstalledLocalDeployer extends AbstractCopyingInstalledLocalDeployer
{
    /** 
     * {@inheritDoc}
     * @see AbstractCopyingInstalledLocalDeployer#AbstractCopyingInstalledLocalDeployer(InstalledLocalContainer)
     */ 
    public Jo1xInstalledLocalDeployer(InstalledLocalContainer container)
    {
        super(container);
    }

    /**
     * Specifies the directory {@link org.codehaus.cargo.container.deployable.Deployable}s should
     * be copied to. For jo! this is the <code>webapp/host</code> directory.
     *
     * @return Deployable directory
     */
    @Override
    public String getDeployableDir()
    {
        return getFileHandler().append(getContainer().getConfiguration().getHome(), "webapp/host");
    }

}
