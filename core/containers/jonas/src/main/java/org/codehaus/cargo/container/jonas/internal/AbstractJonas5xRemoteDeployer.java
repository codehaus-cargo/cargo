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
package org.codehaus.cargo.container.jonas.internal;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;

/**
 * Remote deployer for JOnAS 5.x.
 * 
 * @version $Id$
 */
public abstract class AbstractJonas5xRemoteDeployer extends AbstractJonasRemoteDeployer
{
    /**
     * Constructor.
     * 
     * @param container the remote container
     */
    public AbstractJonas5xRemoteDeployer(RemoteContainer container)
    {
        super(container);
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractJonasRemoteDeployer#getOperationName(ActionType, DeployableType, TargetType)
     */
    @Override
    protected String getOperationName(ActionType actionType, DeployableType deployableType,
        TargetType targetType)
    {
        if (targetType == TargetType.SERVER)
        {
            String operationName;
            if (actionType == ActionType.DEPLOY)
            {
                operationName = "deploy";
            }
            else if (actionType == ActionType.UPLOAD_DEPLOY)
            {
                operationName = "uploadDeploy";
            }
            else if (actionType == ActionType.UNDEPLOY)
            {
                operationName = "undeploy";
            }
            else
            {
                throw new IllegalArgumentException("Unsupported Action type: " + actionType);
            }

            return operationName;
        }
        else if (targetType == TargetType.DOMAIN)
        {
            String operationPrefix;
            if (actionType == ActionType.DEPLOY)
            {
                operationPrefix = "deploy";
            }
            else if (actionType == ActionType.UPLOAD_DEPLOY)
            {
                operationPrefix = "uploadDeploy";
            }
            else if (actionType == ActionType.UNDEPLOY)
            {
                operationPrefix = "unDeploy";
            }
            else
            {
                throw new IllegalArgumentException("Unsupported Action type: " + actionType);
            }

            String operationSuffix;
            if (deployableType == DeployableType.WAR)
            {
                operationSuffix = "War";
            }
            else if (deployableType == DeployableType.EAR)
            {
                operationSuffix = "Ear";
            }
            else if (deployableType == DeployableType.EJB)
            {
                operationSuffix = "Jar";
            }
            else if (deployableType == DeployableType.RAR)
            {
                operationSuffix = "Rar";
            }
            else
            {
                throw new IllegalArgumentException("Unsupported Deployable type: "
                    + deployableType);
            }

            return operationPrefix + operationSuffix;
        }
        else
        {
            throw new IllegalArgumentException("Unsupported Target type: " + targetType);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractJonasRemoteDeployer#getRemoteFileName(Deployable, String, boolean)
     */
    @Override
    protected String getRemoteFileName(Deployable deployable, String deployableIdentifier,
        boolean askFromServer)
    {
        String deployableId = deployableIdentifier;

        if (deployableId != null && deployableId.trim().length() > 0)
        {
            int identifierExtIndex = deployableId.lastIndexOf(".");
            if (identifierExtIndex != -1)
            {
                deployableId = deployableId.substring(0, identifierExtIndex);
            }

            if (deployable.getType() == DeployableType.WAR)
            {
                deployableId += ".war";
            }
            else if (deployable.getType() == DeployableType.EAR)
            {
                deployableId += ".ear";
            }
            else if (deployable.getType() == DeployableType.EJB)
            {
                deployableId += ".jar";
            }
            else if (deployable.getType() == DeployableType.RAR)
            {
                deployableId += ".rar";
            }
            else
            {
                throw new IllegalArgumentException("Unsupported Deployable type: "
                    + deployable.getType());
            }
            return deployableId;
        }

        File localFile = new File(deployable.getFile());
        String localFileName = localFile.getName();
        if (deployable.getType() == DeployableType.WAR)
        {
            WAR war = (WAR) deployable;
            if (war.getContext().length() == 0)
            {
                localFileName = "rootContext.war";
            }
            else
            {
                localFileName = war.getContext() + ".war";
            }
        }

        String result = null;
        if (askFromServer)
        {
            MBeanServerConnectionFactory factory = null;
            try
            {
                factory = getMBeanServerConnectionFactory();
                MBeanServerConnection mbsc = factory.getServerConnection(configuration);
                RemoteDeployerConfig config = getConfig();
    
                ObjectName serverMBeanName = getServerMBeanName(config.getDomainName(), config
                    .getServerName());
    
                List remoteFiles;
                Iterator remoteFilesIterator;
                String lookForFile = "/deploy/" + localFileName;
    
                remoteFiles = (List) mbsc.getAttribute(serverMBeanName, "deployedFiles");
                remoteFilesIterator = remoteFiles.iterator();
                while (remoteFilesIterator.hasNext())
                {
                    String remoteFile = ((String) remoteFilesIterator.next()).replace('\\', '/');
                    if (remoteFile.endsWith(lookForFile))
                    {
                        result = remoteFile;
                        break;
                    }
                }
            }
            catch (Exception ex)
            {
                throw new ContainerException("Undeployment error", ex);
            }
            finally
            {
                if (factory != null)
                {
                    factory.destroy();
                }
            }
        }
        else
        {
            result = localFileName;
        }

        if (result == null)
        {
            throw new ContainerException("Cannot find file \"" + localFileName
                + "\" in JONAS_BASE");
        }
        return result;
    }
}
