/* 
 * ========================================================================
 * 
 * Copyright 2004 Vincent Massol.
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
package org.codehaus.cargo.container.configuration;

import java.util.Map;

/**
 * Represents the capability of a configuration. More specifically it describes the properties that
 * the configuration supports.
 * 
 * @version $Id$
 */
public interface ConfigurationCapability
{
    /**
     * @param propertyName the property for which to verify the support for this configuration
     * @return true if the configuration supports the passed property
     */
    boolean supportsProperty(String propertyName);

    /**
     * @return the list of supported or not suported configuration properties
     */
    Map getProperties();
}
