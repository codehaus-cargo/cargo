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

import org.apache.tools.ant.taskdefs.Java;
import org.codehaus.cargo.container.jonas.Jonas4xInstalledLocalContainer;
import org.codehaus.cargo.util.AntUtils;

/**
 * JOnAS admin command line utils class.
 * 
 * @version $Id$
 */
public class Jonas4xAdmin implements JonasAdmin
{
    /**
     * Target JOnAS container, used for admin command line invocation setup.
     */
    private Jonas4xInstalledLocalContainer targetContainer;

    /**
     * 
     * @param targetContainer the JOnAS target container
     */
    public Jonas4xAdmin(Jonas4xInstalledLocalContainer targetContainer)
    {
        this.targetContainer = targetContainer;
    }

    /**
     * Look if a local server instance is running.
     * 
     * @return true if a local server instance is running
     */
    public final boolean isServerRunning()
    {
        Java java = (Java) new AntUtils().createAntTask("java");
        java.setFork(true);

        targetContainer.doAction(java);
        java.createArg().setValue("org.objectweb.jonas.adm.JonasAdmin");
        targetContainer.doServerAndDomainNameParam(java);
        java.createArg().setValue("-l");

        int returnCode = java.executeJava();
        return returnCode == 0;
    }

    /**
     * Undeploys the given bean name.
     * 
     * @param beanFileName the bean file name
     * @return true if the bean has been correctly undeployed
     */
    public final boolean unDeploy(String beanFileName)
    {
        boolean undeployed = genericDeployment(beanFileName, "-r");
        if (!undeployed)
        {
            // file deployed trough autoload directory are not undeployed it the autoload
            // directory is not specified in the path
            undeployed = genericDeployment("autoload/" + beanFileName, "-r");
        }
        return undeployed;
    }

    /**
     * deploys the given bean name.
     * 
     * @param beanFileName the bean file name
     * @return true if the bean has been correctly deployed
     */
    public final boolean deploy(String beanFileName)
    {
        return genericDeployment(beanFileName, "-a");
    }

    /**
     * 
     * @param beanFileName bean File Name
     * @param deploymentParam deployment parameter
     * @return true if the deployment command(deploy or undeploy...)bean has been correctly executed
     */
    private boolean genericDeployment(String beanFileName, String deploymentParam)
    {
        Java java = (Java) new AntUtils().createAntTask("java");
        java.setFork(true);

        targetContainer.doAction(java);
        java.createArg().setValue("org.objectweb.jonas.adm.JonasAdmin");
        targetContainer.doServerAndDomainNameParam(java);
        java.createArg().setValue(deploymentParam);
        java.createArg().setValue(beanFileName);

        int returnCode = java.executeJava();
        return returnCode == 0;
    }
}
