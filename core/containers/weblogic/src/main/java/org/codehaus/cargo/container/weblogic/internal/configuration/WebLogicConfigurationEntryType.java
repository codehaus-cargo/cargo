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
package org.codehaus.cargo.container.weblogic.internal.configuration;

import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;

/**
 * Represents the type of a resource specific to WebLogic, such as a <code>javax.jms.Server</code>.
 */
public interface WebLogicConfigurationEntryType extends ConfigurationEntryType
{

    /**
     * JMS server.
     */
    String JMS_SERVER = "javax.jms.Server";

    /**
     * JMS module.
     */
    String JMS_MODULE = "javax.jms.Module";

    /**
     * JMS subdeployment.
     */
    String JMS_SUBDEPLOYMENT = "javax.jms.Subdeployment";
}
