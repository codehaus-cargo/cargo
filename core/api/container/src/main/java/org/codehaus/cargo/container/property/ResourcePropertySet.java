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
package org.codehaus.cargo.container.property;

/**
 * Gathers all resource properties valid for all types of containers.
 */
public interface ResourcePropertySet
{
    /**
     * A property to encapsulate all the other resource properties. This is to get around cargo only
     * passing strings around, instead of objects.<br>
     * <b>Important</b>: Please <u>only</u> use this as a setter, as the resources can also be set
     * using the <code>addResource</code> method.
     * @see org.codehaus.cargo.container.configuration.LocalConfiguration#getResources()
     */
    String RESOURCE = "cargo.resource.resource";

    /**
     * The JNDI location that this datasource should be bound do (in the config file). Note that
     * many application servers may prepend a context (typically <code>java:comp/env</code>) to this
     * context. <br>
     */
    String RESOURCE_NAME = "cargo.resource.name";

    /**
     * The type of the resource (ex. <code>javax.sql.XADataSource</code>,
     * <code>javax.sql.ConnectionPoolDataSource</code>, etc.. <br>
     */
    String RESOURCE_TYPE = "cargo.resource.type";

    /**
     * The class name of the Resource implementation class. Example:
     * <code>org.apache.derby.jdbc.EmbeddedXADataSource</code>. <br>
     */
    String RESOURCE_CLASS = "cargo.resource.class";

    /**
     * Unique id to use in configuration files. <br>
     */
    String RESOURCE_ID = "cargo.resource.id";

    /**
     * Parameters passed to the implementation class. <br>
     */
    String PARAMETERS = "cargo.resource.parameters";

}
