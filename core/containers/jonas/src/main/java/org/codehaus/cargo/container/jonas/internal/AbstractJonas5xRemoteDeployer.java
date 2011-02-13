/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
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
import java.io.IOException;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
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
            else if (deployableType == DeployableType.EJB
                || deployableType == DeployableType.BUNDLE)
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
     * @see org.codehaus.cargo.container.jonas.internal.AbstractJonasRemoteDeployer#deploy(Deployable)
     */
    @Override
    public void deploy(Deployable deployable)
    {
        RemoteDeployerConfig config = getConfig();
        if (config.getClusterName() == null)
        {
            MBeanServerConnectionFactory factory = null;
            try
            {
                factory = getMBeanServerConnectionFactory();
                MBeanServerConnection mbsc = factory.getServerConnection(configuration);

                ObjectName depmonitorServiceMBeanName = getDepmonitorServiceMBeanName(
                    config.getDomainName());

                String developmentName = getDeploymentAttributeName(depmonitorServiceMBeanName,
                    mbsc);

                Boolean development = (Boolean) mbsc.getAttribute(depmonitorServiceMBeanName,
                    developmentName);

                mbsc.setAttribute(depmonitorServiceMBeanName, new Attribute(developmentName,
                    Boolean.FALSE));

                try
                {
                    super.deploy(deployable);
                }
                finally
                {
                    mbsc.setAttribute(depmonitorServiceMBeanName, new Attribute(developmentName,
                        development));
                }
            }
            catch (Exception ex)
            {
                if (ex instanceof ContainerException)
                {
                    throw (ContainerException) ex;
                }

                throw new ContainerException("Deployment error", ex);
            }
            finally
            {
                if (factory != null)
                {
                    factory.destroy();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.cargo.container.jonas.internal.AbstractJonasRemoteDeployer#undeploy(Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        RemoteDeployerConfig config = getConfig();
        if (config.getClusterName() == null)
        {
            MBeanServerConnectionFactory factory = null;
            try
            {
                factory = getMBeanServerConnectionFactory();
                MBeanServerConnection mbsc = factory.getServerConnection(configuration);

                ObjectName depmonitorServiceMBeanName = getDepmonitorServiceMBeanName(
                    config.getDomainName());

                String developmentName = getDeploymentAttributeName(depmonitorServiceMBeanName,
                    mbsc);

                Boolean development = (Boolean) mbsc.getAttribute(depmonitorServiceMBeanName,
                    developmentName);

                mbsc.setAttribute(depmonitorServiceMBeanName, new Attribute(developmentName,
                    Boolean.FALSE));

                try
                {
                    super.undeploy(deployable);

                    if (development)
                    {
                        String remoteFileName = findRemoteFileName(deployable,
                            config.getDeployableIdentifier(), true);

                        if (remoteFileName != null)
                        {
                            getLogger().info(
                                "The target JOnAS server is running in development mode. "
                                + "CARGO will now delete the undeployed module.",
                                this.getClass().getName());

                            ObjectName serverMBeanName = getServerMBeanName(config.getDomainName(),
                                config.getServerName());

                            mbsc.invoke(serverMBeanName, "removeModuleFile", new Object[]
                            {
                                remoteFileName
                            }, new String[]
                            {
                                String.class.getName()
                            });
                        }
                    }
                }
                finally
                {
                    mbsc.setAttribute(depmonitorServiceMBeanName, new Attribute(developmentName,
                        development));
                }
            }
            catch (Exception ex)
            {
                if (ex instanceof ContainerException)
                {
                    throw (ContainerException) ex;
                }

                throw new ContainerException("Deployment error", ex);
            }
            finally
            {
                if (factory != null)
                {
                    factory.destroy();
                }
            }
        }
    }

    /**
     * Get the depmonitor service MBean.
     *
     * @param domainName domain Name
     * @return the depmonitor service MBean Name
     * @throws MalformedObjectNameException throwing when object name is wrong
     */
    protected ObjectName getDepmonitorServiceMBeanName(String domainName)
        throws MalformedObjectNameException
    {
        if (domainName == null || domainName.trim().length() == 0)
        {
            throw new MalformedObjectNameException("Empty domain name provided");
        }

        return new ObjectName(domainName + ":type=service,name=depmonitor");
    }

    /**
     * Get the attribute name for the "development" attribute. That name depends on the exact
     * server version (<code>developmentMode</code> on JonAS 5.0.x and 5.1.x,
     * <code>development</code> on JOnAS 5.2.x and afterwards).
     *
     * @param depmonitor Object name of the depmonitor service.
     * @param mbsc MBean server connection.
     * @return the attribute name for the "development" attribute.
     * @throws JMException MBean exception.
     * @throws IOException Communication exception.
     */
    protected String getDeploymentAttributeName(ObjectName depmonitor, MBeanServerConnection mbsc)
        throws JMException, IOException
    {
        try
        {
            // JOnAS 5.0.x and 5.1.x
            mbsc.getAttribute(depmonitor, "developmentMode");
            return "developmentMode";
        }
        catch (AttributeNotFoundException e)
        {
            // JOnAS 5.2.x
            mbsc.getAttribute(depmonitor, "development");
            return "development";
        }
    }

    /**
     * Finds a deployable file on the remote server.
     * @param deployable Deployable to look for.
     * @param deployableIdentifier Deployable identifier.
     * @param askFromServer Whether to ask from server, only <code>false</code> during tests.
     * @return String if found, <code>null</code> if nothing found.
     */
    protected String findRemoteFileName(Deployable deployable, String deployableIdentifier,
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
    
                List<String> remoteFiles;
                String lookForFile = "/deploy/" + localFileName;
    
                remoteFiles = (List<String>) mbsc.getAttribute(serverMBeanName, "deployedFiles");
                for (String remoteFile : remoteFiles)
                {
                    remoteFile = remoteFile.replace('\\', '/');
                    if (remoteFile.endsWith(lookForFile))
                    {
                        result = remoteFile;
                        break;
                    }
                }
            }
            catch (Exception ex)
            {
                throw new ContainerException("Failed looking for deployable" + deployable, ex);
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

        return result;
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
        String result = this.findRemoteFileName(deployable, deployableIdentifier, askFromServer);
        if (result == null)
        {
            throw new ContainerException("Cannot find deployable " + deployable
                + " in JONAS_BASE");
        }
        return result;
    }
}
