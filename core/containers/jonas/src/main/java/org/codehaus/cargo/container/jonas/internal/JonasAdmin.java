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

/**
 * JOnAS admin command line capabilities interface.
 * 
 * @version $Id: JonasAdmin.java 14631 2008-07-24 13:29:15Z alitokmen $
 */
public interface JonasAdmin
{
    /**
     * Look if a local server instance is running.
     * 
     * @return true if a local server instance is running
     */
    boolean isServerRunning();

    /**
     * Undeploys the given bean name.
     * 
     * @param beanFileName the bean file name
     * @return true if the bean has been correctly undeployed
     */
    boolean unDeploy(String beanFileName);

    /**
     * deploys the given bean name.
     * 
     * @param beanFileName the bean file name
     * @return true if the bean has been correctly undeployed
     */
    boolean deploy(String beanFileName);
}
