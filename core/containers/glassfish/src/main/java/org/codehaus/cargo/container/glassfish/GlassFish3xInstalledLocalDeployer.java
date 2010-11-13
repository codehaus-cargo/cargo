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
package org.codehaus.cargo.container.glassfish;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.util.DefaultFileHandler;

/**
 * GlassFish 3.x installed local deployer, which deploys OSGi bundles manually and uses the
 * standard asadmin-based deployment for other deployables.
 * 
 * @version $Id$
 */
public class GlassFish3xInstalledLocalDeployer extends GlassFish2xInstalledLocalDeployer
{

    /**
     * File handler.
     */
    private DefaultFileHandler fileHandler = new DefaultFileHandler();

    /**
     * Calls parent constructor, which saves the container.
     *
     * @param localContainer Container.
     */
    public GlassFish3xInstalledLocalDeployer(InstalledLocalContainer localContainer)
    {
        super(localContainer);

        this.fileHandler = new DefaultFileHandler();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doDeploy(Deployable deployable, boolean overwrite)
    {
        if (DeployableType.BUNDLE.equals(deployable.getType()))
        {
            String bundlePath = this.getBundleDeployablePath(deployable);
            this.fileHandler.copyFile(deployable.getFile(), bundlePath, overwrite);
        }
        else
        {
            super.doDeploy(deployable, overwrite);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        if (DeployableType.BUNDLE.equals(deployable.getType()))
        {
            String bundlePath = this.getBundleDeployablePath(deployable);
            this.fileHandler.delete(bundlePath);
        }
        else
        {
            super.undeploy(deployable);
        }
    }

    /**
     * @param bundle Bundle to deploy
     * @return The file name of the bundle in the GlassFish 3.x bundle autodeploy path.
     */
    protected String getBundleDeployablePath(Deployable bundle)
    {
        GlassFishStandaloneLocalConfiguration configuration =
            (GlassFishStandaloneLocalConfiguration) this.getLocalContainer().getConfiguration();

        String bundleDeployPath = configuration.getHome() + "/"
            + configuration.getPropertyValue(GlassFishPropertySet.DOMAIN_NAME)
            + "/autodeploy/bundles";
        this.fileHandler.mkdirs(bundleDeployPath);

        return bundleDeployPath + "/" + this.fileHandler.getName(bundle.getFile());
    }

}
