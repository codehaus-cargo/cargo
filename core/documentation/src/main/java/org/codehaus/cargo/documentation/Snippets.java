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
package org.codehaus.cargo.documentation;

import java.net.URL;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.container.resin.Resin3xInstalledLocalContainer;
import org.codehaus.cargo.container.resin.Resin3xStandaloneLocalConfiguration;
import org.codehaus.cargo.container.resin.ResinInstalledLocalDeployer;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;

/**
 * Code snippets referenced from the Cargo website using the Confluence <code>{snippet}</code> marco
 * (see http://confluence.atlassian.com/display/CONFEXT/Snippet+Macro+Library).
 * 
 * WARNING1: the snippet id must NOT contain the words "start" or "end".
 * 
 * WARNING2: nested snippet ids must not contain parent's id. For example a nested
 * "configuration-typed-resin3x" is invalid if the parent has an id of "resin3x" or "configuration"
 * or "typed-resin3x".
 * 
 */
public class Snippets
{
    /**
     * Snippet for starting and stopping a Resin 3.x container.
     */
    public void snippetResin3xStartAndStopTyped()
    {
        // START SNIPPET: qs-typed
        Deployable war = new WAR("path/to/simple.war");

        // START SNIPPET: configuration-typed-resin3x
        LocalConfiguration configuration =
            new Resin3xStandaloneLocalConfiguration("target/myresin3x");
        // END SNIPPET: configuration-typed-resin3x
        configuration.addDeployable(war);

        InstalledLocalContainer container =
            new Resin3xInstalledLocalContainer(configuration);
        container.setHome("c:/apps/resin-3.0.18");

        container.start();
        // Here you are assured the container is started.

        container.stop();
        // Here you are assured the container is stopped.
        // END SNIPPET: qs-typed
    }

    /**
     * Snippet for starting and stopping a Resin 3.x container.
     */
    public void snippetResin3xStartAndStopUntyped()
    {
        // START SNIPPET: qs-untyped
        Deployable war = new DefaultDeployableFactory().createDeployable(
            "resin3x", "path/to/simple.war", DeployableType.WAR);

        // START SNIPPET: configuration-untyped-resin3x
        ConfigurationFactory configurationFactory =
            new DefaultConfigurationFactory();
        LocalConfiguration configuration =
            (LocalConfiguration) configurationFactory.createConfiguration(
                "resin3x", ContainerType.INSTALLED, ConfigurationType.STANDALONE);
        // END SNIPPET: configuration-untyped-resin3x
        configuration.addDeployable(war);

        InstalledLocalContainer container =
            (InstalledLocalContainer) new DefaultContainerFactory().createContainer(
                "resin3x", ContainerType.INSTALLED, configuration);
        container.setHome("c:/apps/resin-3.0.18");

        container.start();
        // Here you are assured the container is started.

        container.stop();
        // Here you are assured the container is stopped.
        // END SNIPPET: qs-untyped
    }

    /**
     * Snippet for local deployment on a Resin 3.x container.
     * @throws Exception If the snippet throws an exception.
     */
    public void snippetResin3xLocalDeploy() throws Exception
    {
        // START SNIPPET: qs-deploy
        InstalledLocalContainer container = new Resin3xInstalledLocalContainer(
            new Resin3xStandaloneLocalConfiguration("target/myresin3x"));
        container.setHome("c:/apps/resin-3.0.18");

        container.start();

        // Here you are assured the container is started.

        Deployable war = new WAR("path/to/simple.war");
        Deployer deployer = new ResinInstalledLocalDeployer(container);
        deployer.deploy(war);

        // Here you are NOT sure the WAR has finished deploying. To be sure you
        // need to use a DeployableMonitor to monitor the deployment. For example
        // the following code deploys the WAR and wait until it is available to
        // serve requests (the URL should point to a resource inside your WAR):
        deployer.deploy(war, new URLDeployableMonitor(
            new URL("http://server:port/some/url")));

        container.stop();

        // Here you are assured the container is stopped.
        // END SNIPPET: qs-deploy
    }
}
