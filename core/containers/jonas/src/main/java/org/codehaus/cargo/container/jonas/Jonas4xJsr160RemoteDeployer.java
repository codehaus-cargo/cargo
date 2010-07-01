/* 
 * ========================================================================
 * 
 * Copyright 2007-2008 OW2. Code from this file was originally imported
 * from the OW2 JOnAS project.
 * 
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
import org.codehaus.cargo.container.jonas.internal.AbstractJonas4xRemoteDeployer;
import org.codehaus.cargo.container.jonas.internal.JSR160MBeanServerConnectionFactory;
import org.codehaus.cargo.container.jonas.internal.MBeanServerConnectionFactory;

/**
 * Remote deployer that uses JMX Remoting (JSR 160) to deploy to JOnAS.
 * 
 * @version $Id$
 */
public class Jonas4xJsr160RemoteDeployer extends AbstractJonas4xRemoteDeployer
{
    /**
     * Constructor.
     * 
     * @param container the remote container
     */
    public Jonas4xJsr160RemoteDeployer(RemoteContainer container)
    {
        super(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MBeanServerConnectionFactory getMBeanServerConnectionFactory()
    {
        return new JSR160MBeanServerConnectionFactory();
    }
}
