/* 
 * ========================================================================
 * 
 * Copyright 2006 Vincent Massol.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ========================================================================
 */
package org.codehaus.cargo.container.configuration.builder;

import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;


/**
 * Builds String representations a Resource configuration. These may be JSON or
 * XML or Properties that the <code>Container</code> will know what to do with.
 * 
 * @version $Id$
 */
public interface ConfigurationBuilder
{
    /**
     * Detects the type of the <code>Resource</code> and creates an appropriate configuration.
     * 
     * @param resource the Resource you wish to build a configuration entry for.
     * @return the container-specific representation of this configuration.
     */
    String toConfigurationEntry(Resource resource);

    /**
     * Detects the type of the <code>DataSource</code> and creates an appropriate configuration.
     * 
     * @param ds the DataSource you wish to build a configuration entry for.
     * @return the container-specific representation of this configuration.
     */
    String toConfigurationEntry(DataSource ds);

}
