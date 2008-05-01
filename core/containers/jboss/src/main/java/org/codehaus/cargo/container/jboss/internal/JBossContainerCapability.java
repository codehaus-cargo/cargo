/*
 * ========================================================================
 * Copyright 2005 Vincent Massol.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * ========================================================================
 */
package org.codehaus.cargo.container.jboss.internal;

import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.internal.J2EEContainerCapability;

/**
 * Capabilities of the JBoss container.
 * 
 * @version $Id$
 */
public class JBossContainerCapability extends J2EEContainerCapability
{
    /**
     * Add support for EJB deployable types.
     * {@inheritDoc}
     * @see J2EEContainerCapability#supportsDeployableType(DeployableType)
     */
    public boolean supportsDeployableType(DeployableType type)
    {
        return (type == DeployableType.EJB || type == DeployableType.SAR ) || super.supportsDeployableType(type);
    }

}
