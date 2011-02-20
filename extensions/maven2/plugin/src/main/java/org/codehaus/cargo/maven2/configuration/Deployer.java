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
package org.codehaus.cargo.maven2.configuration;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.generic.deployer.DefaultDeployerFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;

/**
 * Holds configuration data for the <code>&lt;deployer&gt;</code> tag used to configure the plugin
 * in the <code>pom.xml</code> file.
 * 
 * @version $Id$
 */
public class Deployer
{
    private String type;

    private String implementation;

    private Deployable[] deployables;

    public String getImplementation()
    {
        return this.implementation;
    }

    public void setImplementation(String implementation)
    {
        this.implementation = implementation;
    }

    public String getType()
    {
        return this.type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Deployable[] getDeployables()
    {
        return this.deployables;
    }

    public org.codehaus.cargo.container.deployer.Deployer createDeployer(
        org.codehaus.cargo.container.Container container) throws MojoExecutionException
    {
        org.codehaus.cargo.container.deployer.Deployer deployer;
        DeployerFactory factory = new DefaultDeployerFactory();

        if (getType() == null)
        {
            if (getImplementation() != null)
            {
                throw new MojoExecutionException("As you have specified a deployer implementation "
                    + "to register you need to specify a deployer type in the deployer "
                    + "configuration in the POM.");
            }

            // Use a deployer matching the container's type if none is specified.
            // @see DeployerFactory#createDeployer(Container)
            deployer = factory.createDeployer(container);
        }
        else
        {
            DeployerType type = DeployerType.toType(getType());

            // If the user has registered a custom deployer class, register it against the
            // default deployer factory.
            if (getImplementation() != null)
            {
                try
                {
                    Class deployerClass = Class.forName(getImplementation(), true,
                        this.getClass().getClassLoader());
                    factory.registerDeployer(container.getId(), type, deployerClass);
                }
                catch (ClassNotFoundException cnfe)
                {
                    throw new MojoExecutionException("Custom deployer implementation ["
                        + getImplementation() + "] cannot be loaded", cnfe);
                }
            }

            deployer = factory.createDeployer(container, type);
        }

        return deployer;
    }
}
