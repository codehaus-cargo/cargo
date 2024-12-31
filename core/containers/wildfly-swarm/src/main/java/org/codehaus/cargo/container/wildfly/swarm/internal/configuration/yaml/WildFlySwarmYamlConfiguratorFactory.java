/*
 * ========================================================================
 *
 *  Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  ========================================================================
 */
package org.codehaus.cargo.container.wildfly.swarm.internal.configuration.yaml;

import java.io.Flushable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import java.nio.charset.StandardCharsets;
import org.codehaus.cargo.container.wildfly.swarm.internal.configuration.ConfigurationContext;
import org.codehaus.cargo.container.wildfly.swarm.internal.configuration.UserAccountsConfigurator;
import org.codehaus.cargo.container.wildfly.swarm.internal.configuration.WildFlySwarmConfiguratorFactory;

/**
 * WildFly Swarm yaml configuration factory. Writes configuration changes to project-{name}.yaml
 * file.
 */
public final class WildFlySwarmYamlConfiguratorFactory implements
        WildFlySwarmConfiguratorFactory, Flushable
{
    /**
     * Configuration context makes accessible data and utilities from configuration.
     */
    private final ConfigurationContext configurationContext;

    /**
     * YAML generator instance for creating YAML content.
     */
    private final YAMLGenerator yamlGenerator;

    /**
     * User accounts configuration implementation.
     */
    private UserAccountsConfigurator userAccountsYamlConfigurator;


    /**
     * Creates new instance of this factory class.
     * @param configurationContext configuration context.
     */
    public WildFlySwarmYamlConfiguratorFactory(ConfigurationContext configurationContext)
    {
        this.configurationContext = configurationContext;
        this.yamlGenerator = createYamlGenerator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserAccountsConfigurator userAccountsConfigurator()
    {
        if (userAccountsYamlConfigurator == null)
        {
            userAccountsYamlConfigurator =
                    new UserAccountsYamlConfigurator(configurationContext, yamlGenerator);
        }
        return userAccountsYamlConfigurator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush()
    {
        boolean isEmpty = false;
        try
        {
            yamlGenerator.flush();
            String yamlContent = yamlGenerator.getOutputTarget().toString();

            if (yamlContent != null && !yamlContent.isEmpty())
            {
                configurationContext.getFileHandler().writeTextFile(
                    configurationContext.getProjectDescriptor().getAbsolutePath(),
                        yamlContent, StandardCharsets.UTF_8);
            }
            else
            {
                isEmpty = true;
            }
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Unable to flush YAML generator.", ex);
        }
        finally
        {
            if (!isEmpty)
            {
                try
                {
                    yamlGenerator.close();
                }
                catch (IOException ex)
                {
                    throw new RuntimeException("Unable to close YAML generator.", ex);
                }
            }
        }

    }

    /**
     * Creates YAML generator instance.
     * @return YAMLGenerator instance.
     */
    private YAMLGenerator createYamlGenerator()
    {
        final Writer writer = new StringWriter();

        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true);
        yamlFactory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);

        try
        {
            return yamlFactory.createGenerator(writer);
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Cannot create YAML generator.", ex);
        }
    }
}
