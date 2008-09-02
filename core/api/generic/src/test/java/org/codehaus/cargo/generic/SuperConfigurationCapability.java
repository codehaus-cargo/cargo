/*
 * ========================================================================
 *
 * Copyright 2006-2008 Vincent Massol.
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
package org.codehaus.cargo.generic;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;

import java.util.HashMap;
import java.util.Map;

/**
 * Used by {@link TestFactoryRegistry} to see if the discovery succeeds.
 *
 * @version $Id$
 */
public class SuperConfigurationCapability implements ConfigurationCapability
{
    public boolean supportsProperty(String propertyName)
    {
        return true;
    }

    public Map getProperties()
    {
        return new HashMap();
    }
}
