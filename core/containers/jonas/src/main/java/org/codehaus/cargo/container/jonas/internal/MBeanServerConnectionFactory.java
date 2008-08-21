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
package org.codehaus.cargo.container.jonas.internal;

import javax.management.MBeanServerConnection;

import org.codehaus.cargo.container.configuration.RuntimeConfiguration;

/**
 * Factory to create a remote JMX MBean server connection.
 * 
 * @version $Id: MBeanServerConnectionFactory.java 14641 2008-07-25 11:46:29Z alitokmen $
 */
public interface MBeanServerConnectionFactory
{
    /**
     * Create a new MBean server connection.
     * 
     * @param configuration Runtime Configuration
     * @return a MBeanServerConnection
     * @throws Exception if the connection cannot be done
     */
    MBeanServerConnection getServerConnection(RuntimeConfiguration configuration) throws Exception;

    /**
     * Destroys the factory and any underlying IO sockets.
     */
    void destroy();
}
