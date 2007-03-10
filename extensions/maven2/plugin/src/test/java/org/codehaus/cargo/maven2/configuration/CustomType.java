/*
 * ========================================================================
 *
 * Copyright 2005-2006 Vincent Massol.
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

public class CustomType implements Deployable
{
    public CustomType(String file)
    {
        // Voluntarily empty for testing
    }

    public String getFile()
    {
        return null;
    }

    public DeployableType getType()
    {
        return DeployableType.toType("customType");
    }

    public Logger getLogger()
    {
        return null;
    }

    public void setLogger(Logger logger)
    {
        // Voluntarily empty for testing
    }
}
