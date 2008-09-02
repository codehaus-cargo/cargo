/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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

import org.codehaus.cargo.util.log.Loggable;

import java.util.Map;

/**
 * A configuration represents how a container is configured: where deployables are deployed,
 * container ports, logging levels, container authentication, etc. Cargo supports several 
 * types of configuration:
 * <ul>
 *   <li>
 *     <b>local configuration</b>: it represents a configuration located somewhere on the local
 *                                 file system. A local configuration is activated before the
 *                                 container is started.
 *   </li>
 *   <li>
 *     <b>runtime configuration</b>: it represents a configuration for a container that is 
 *                                   already started. The settings that can be set/unset depend
 *                                   on the container's capability for dynamically changing
 *                                   settings.
 *   </li>
 * </ul> 
 *  
 * @version $Id$
 */
public interface Configuration extends Loggable
{
    /**
     * A property is a configuration value for the container (eg the web port, the number of 
     * executing threads, etc).
     * 
     * @param name the property name
     * @param value the property value
     */
    void setProperty(String name, String value);

    /**
     * @return the list of properties set
     * @see #setProperty(String, String)
     */
    Map getProperties();

    /**
     * @param name the property name for which to return the value
     * @return the property's value
     * @see #setProperty(String, String)
     */
    String getPropertyValue(String name);

    /**
     * @return the {@link ConfigurationCapability} of the configuration in term of properties it 
     *         supports, etc
     */
    ConfigurationCapability getCapability();
    
    /**
     * @return the configuration type (standalone, existing, runtime, etc)
     */
    ConfigurationType getType();
}
