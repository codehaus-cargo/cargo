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
package org.codehaus.cargo.container.glassfish;

import java.util.Properties;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.glassfish.internal.AbstractGlassFishInstalledLocalDeployer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.util.CargoException;

/**
 * GlassFish 5.x installed local container.
 */
public class GlassFish5xInstalledLocalContainer extends GlassFish4xInstalledLocalContainer
{

    /**
     * Calls parent constructor, which saves the configuration.
     * 
     * @param localConfiguration Configuration.
     */
    public GlassFish5xInstalledLocalContainer(LocalConfiguration localConfiguration)
    {
        super(localConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractGlassFishInstalledLocalDeployer getLocalDeployer()
    {
        return new GlassFish5xInstalledLocalDeployer(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStart(JvmLauncher java) throws Exception
    {
        super.doStart(java);

        // Since Java EE 7, every application server should support the default resources,
        // including a datasource. If we have removed it, then make sure the first datasource is
        // set as default, else Glassfish 5.x onwards will get confused.
        if (this.getConfiguration() instanceof StandaloneLocalConfiguration
            && Boolean.parseBoolean(this.getConfiguration().getPropertyValue(
                GlassFishPropertySet.REMOVE_DEFAULT_DATASOURCE))
            && this.getConfiguration().getDataSources().size() > 0)
        {
            try
            {
                // CARGO-1599: Only redeploy a default datasource if
                //             the container configuration doesn't define one
                boolean configurationContainsDefaultDatasource = false;
                for (DataSource datasource : this.getConfiguration().getDataSources())
                {
                    if ("jdbc/__default".equals(datasource.getJndiLocation()))
                    {
                        configurationContainsDefaultDatasource = true;
                        break;
                    }
                }
                if (!configurationContainsDefaultDatasource)
                {
                    AbstractGlassFishInstalledLocalDeployer deployer = getLocalDeployer();

                    DataSource firstDS = this.getConfiguration().getDataSources().get(0);
                    DataSource dataSource = new DataSource("jdbc/__default",
                        firstDS.getConnectionType(), firstDS.getTransactionSupport(),
                            firstDS.getDriverClass(), firstDS.getUrl(), firstDS.getUsername(),
                                firstDS.getPassword(), "DummyCargoDefaultDS-" + firstDS.getId(),
                                    new Properties(firstDS.getConnectionProperties()));

                    deployer.deployDatasource(dataSource);
                }
            }
            catch (Throwable t)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("At least one GlassFish deployment has failed: ");
                sb.append(t.toString());

                try
                {
                    this.stop();
                }
                catch (Throwable stopException)
                {
                    sb.append("; moreover stopping the container has also failed: ");
                    sb.append(stopException.toString());
                }

                throw new CargoException(sb.toString(), t);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId()
    {
        return "glassfish5x";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "GlassFish " + getVersion("5.x");
    }

}
