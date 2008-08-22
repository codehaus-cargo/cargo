/* 
 * ========================================================================
 * 
 * Copyright 2007-2008 OW2.
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
package org.codehaus.cargo.container.jonas;

import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.jonas.internal.AbstractJonas5xRemoteDeployer;
import org.codehaus.cargo.container.jonas.internal.MBeanServerConnectionFactory;
import org.codehaus.cargo.container.jonas.internal.MEJBMBeanServerConnectionFactory;

/**
 * Remote deployer that uses Managment EJB (MEJB) to deploy to JOnAS.
 * 
 * @version $Id$
 */
public class Jonas5xMEJBRemoteDeployer extends AbstractJonas5xRemoteDeployer
{
    /**
     * Constructor.
     * 
     * @param container the remote container
     */
    public Jonas5xMEJBRemoteDeployer(RemoteContainer container)
    {
        super(container);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.jonas.internal#getMBeanServerConnectionFactory()
     */
    public MBeanServerConnectionFactory getMBeanServerConnectionFactory()
    {
        return new MEJBMBeanServerConnectionFactory();
    }
}
