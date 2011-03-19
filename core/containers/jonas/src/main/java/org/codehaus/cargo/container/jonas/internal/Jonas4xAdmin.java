/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
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
package org.codehaus.cargo.container.jonas.internal;

/**
 * JOnAS 4X admin command line utils interface.
 * 
 * @version $Id$
 */
public interface Jonas4xAdmin
{
    /**
     * Look if a local server instance is running.
     * 
     * @param command Command to execute, for example <code>ping</code> (to check if server is
     * started), <code>j</code> (to check if JNDI is accessible), etc.
     * @param expectedReturnCode Expected return code.
     * @return true if the command matches the result
     */
    boolean isServerRunning(String command, int expectedReturnCode);

    /**
     * Undeploys the given bean name.
     * 
     * @param beanFileName the bean file name
     * @return true if the bean has been correctly undeployed
     */
    boolean unDeploy(final String beanFileName);

    /**
     * deploys the given bean name.
     * 
     * @param beanFileName the bean file name
     * @return true if the bean has been correctly deployed
     */
    boolean deploy(final String beanFileName);
}
