/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
package org.codehaus.cargo.maven2.configuration;

import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.util.log.Logger;

/**
 * Dummy {@link Deployable} implementation.
 * 
 * @version $Id$
 */
public class CustomType implements Deployable
{
    /**
     * Empty constructor.
     * @param file Ignored.
     */
    public CustomType(String file)
    {
        // Voluntarily empty for testing
    }

    /**
     * @return <code>null</code>
     */
    public String getFile()
    {
        return null;
    }

    /**
     * @return {@link DeployableType#toType(String)}<code>("customType")</code>
     */
    public DeployableType getType()
    {
        return DeployableType.toType("customType");
    }

    /**
     * @return <code>null</code>
     */
    public Logger getLogger()
    {
        return null;
    }

    /**
     * @param logger Ignored.
     */
    public void setLogger(Logger logger)
    {
        // Voluntarily empty for testing
    }

    /**
     * @return <code>false</code>
     */
    public boolean isExpanded()
    {
        return false;
    }
}
