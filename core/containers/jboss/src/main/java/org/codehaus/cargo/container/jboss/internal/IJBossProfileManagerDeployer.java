/*
 * ========================================================================
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
package org.codehaus.cargo.container.jboss.internal;

import java.io.File;

/**
 * The generic interface for a JBoss deployer that uses the JBoss profile manager.
 * 
 * @version $Id$
 */
public interface IJBossProfileManagerDeployer
{

    /**
     * Deploys an application.
     * 
     * @param deploymentFile File to deploy from local.
     * @param deploymentName Name of the deployment on the remote server.
     * @throws Exception If anything bad occurs.
     */
    void deploy(File deploymentFile, String deploymentName) throws Exception;

    /**
     * Undeploys an application.
     * 
     * @param deploymentName Name of the deployment on the remote server.
     * @throws Exception If anything bad occurs.
     */
    void undeploy(String deploymentName) throws Exception;

}
