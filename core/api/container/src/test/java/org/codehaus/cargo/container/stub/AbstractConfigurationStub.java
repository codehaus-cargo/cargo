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
package org.codehaus.cargo.container.stub;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.NullLogger;

import java.util.Map;
import java.util.HashMap;

/**
 * Code common to all configuration stubs.
 *
 * @version $Id$
 */
public abstract class AbstractConfigurationStub implements Configuration
{
    private Map properties = new HashMap();

    public void setLogger(Logger logger)
    {
        // Voluntarily not doing anything for testing
    }

    public Logger getLogger()
    {
        return new NullLogger();
    }

    public ConfigurationCapability getCapability()
    {
        throw new RuntimeException("Not implemented");
    }

    public void setProperty(String name, String value)
    {
        this.properties.put(name, value);
    }

    public Map getProperties()
    {
        return this.properties;
    }

    public String getPropertyValue(String name)
    {
        return (String) this.properties.get(name);
    }
}
