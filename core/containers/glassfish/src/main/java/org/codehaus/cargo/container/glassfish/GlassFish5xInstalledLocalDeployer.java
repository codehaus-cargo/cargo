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
package org.codehaus.cargo.container.glassfish;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.container.InstalledLocalContainer;

/**
 * GlassFish 5.x installed local deployer, which uses the GlassFish asadmin to deploy and undeploy
 * applications.
 */
public class GlassFish5xInstalledLocalDeployer extends GlassFish4xInstalledLocalDeployer
{
    /**
     * Calls parent constructor, which saves the container.
     * 
     * @param localContainer Container.
     */
    public GlassFish5xInstalledLocalDeployer(InstalledLocalContainer localContainer)
    {
        super(localContainer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void undeployDatasource(String poolName, String jdbcName)
    {
        List<String> args = new ArrayList<String>();

        try
        {
            args.clear();
            this.addConnectOptions(args);
            args.add("delete-jdbc-resource");
            args.add(jdbcName);
            this.getLocalContainer().invokeAsAdmin(false, args);

            args.clear();
            this.addConnectOptions(args);
            args.add("delete-jdbc-connection-pool");
            args.add(poolName);
            this.getLocalContainer().invokeAsAdmin(false, args);
        }
        catch (Throwable t)
        {
            // In some cases, asadmin returns an error:
            //   The jdbc/__default resource cannot be deleted
            //   as it is required to be configured in the system.
            // In that case, delete the pool with the cascade option
            args.clear();
            this.addConnectOptions(args);
            args.add("delete-jdbc-connection-pool");
            args.add("--cascade=true");
            args.add(poolName);
            this.getLocalContainer().invokeAsAdmin(false, args);
        }
    }
}
