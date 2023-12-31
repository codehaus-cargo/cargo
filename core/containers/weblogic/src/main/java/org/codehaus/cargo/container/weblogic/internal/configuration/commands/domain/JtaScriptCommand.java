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
package org.codehaus.cargo.container.weblogic.internal.configuration.commands.domain;

import java.util.Map;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.script.AbstractResourceScriptCommand;
import org.codehaus.cargo.container.weblogic.WebLogicPropertySet;

/**
 * Implementation of setting server JTA properties configuration script command.
 */
public class JtaScriptCommand extends AbstractResourceScriptCommand
{
    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     * @param resourcePath Path to configuration script resources.
     */
    public JtaScriptCommand(Configuration configuration, String resourcePath)
    {
        super(configuration, resourcePath);
    }

    @Override
    protected String getScriptRelativePath()
    {
        return "domain/jta.py";
    }

    @Override
    protected void addConfigurationScriptProperties(Map<String, String> propertiesMap)
    {
        LocalConfiguration configuration = (LocalConfiguration) getConfiguration();
        String domainName = configuration.getFileHandler().getName(configuration.getHome());
        propertiesMap.put("cargo.weblogic.domain.name", domainName);
    }

    @Override
    public boolean isApplicable()
    {
        boolean transactionTimeoutSet = getConfiguration().
                getPropertyValue(WebLogicPropertySet.JTA_TRANSACTION_TIMEOUT) != null;

        return transactionTimeoutSet;
    }
}
