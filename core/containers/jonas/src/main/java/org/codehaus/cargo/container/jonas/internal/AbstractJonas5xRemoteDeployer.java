/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.jonas.JonasPropertySet;

/**
 * Remote deployer for JOnAS 5.x.
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
            catch (Throwable t)
            {
                if (t instanceof ContainerException)
                {
                    throw (ContainerException) t;
                }

                throw new ContainerException("Deployment error", t);
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
            catch (Throwable t)
            {
                if (t instanceof ContainerException)
                {
                    throw (ContainerException) t;
                }

                throw new ContainerException("Deployment error: " + t.getMessage(), t);
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
        if (domainName == null || domainName.trim().isEmpty())
        {
            throw new MalformedObjectNameException("Empty domain name provided");
        }

        return new ObjectName(domainName + ":type=service,name=depmonitor");
    }

    /**
     * Get the attribute name for the "development" attribute. That name depends on the exact server
     * version (<code>developmentMode</code> on JonAS 5.0.x and 5.1.x, <code>development</code> on
     * JOnAS 5.2.x and afterwards).
     * 
     * @param depmonitor Object name of the depmonitor service.
     * @param mbsc MBean server connection.
     * @return the attribute name for the "development" attribute.
     * @throws Throwable If anything fails.
     */
    protected String getDeploymentAttributeName(ObjectName depmonitor, MBeanServerConnection mbsc)
        throws Throwable
    {
        try
        {
            // JOnAS 5.0.x and 5.1.x
            mbsc.getAttribute(depmonitor, "developmentMode");
            return "developmentMode";
        }
        catch (Throwable t)
        {
            // The AttributeNotFoundException is directly t if there is no JMX security,
            // else it is deeper embedded somewhere on some versions due to bug JONAS-385
            Throwable cause = t;
            while (cause != null)
            {
                if (cause instanceof AttributeNotFoundException)
                {
                    break;
                }
                else
                {
                    cause = cause.getCause();
                }
            }
            if (cause != null && cause instanceof AttributeNotFoundException)
            {
                // JOnAS 5.2.x
                mbsc.getAttribute(depmonitor, "development");
                return "development";
            }
            else
            {
                throw t;
            }
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
        getLogger().debug("Finding remote file name for deployable " + deployable
            + " with deployable identifier " + deployableIdentifier + " and ask from server "
            + askFromServer, this.getClass().getName());

        String deployableId = deployableIdentifier;

        if (deployableId != null && !deployableId.trim().isEmpty())
        {
            int identifierExtIndex = deployableId.lastIndexOf('.');
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
            if (war.getContext().isEmpty())
            {
                localFileName = "rootContext.war";
            }
            else
            {
                localFileName = war.getContext() + ".war";
            }
        }
        else if (deployable.getType() == DeployableType.FILE)
        {
            if (localFileName.endsWith(".pom"))
            {
                localFileName = localFileName.substring(0,
                    localFileName.length() - 3) + "xml";
            }
        }

        String result = null;
        if (askFromServer)
        {
            MBeanServerConnectionFactory factory = null;
            try
            {
                // Only look for deployables in JONAS_BASE/deploy
                String lookForFile = "/deploy/" + localFileName;

                factory = getMBeanServerConnectionFactory();
                MBeanServerConnection mbsc = factory.getServerConnection(configuration);
                RemoteDeployerConfig config = getConfig();

                ObjectName serverMBeanName = getServerMBeanName(config.getDomainName(), config
                    .getServerName());

                List<String> remoteFiles = (List<String>)
                    mbsc.getAttribute(serverMBeanName, "deployedFiles");

                remoteFiles.addAll((List<String>)
                    mbsc.getAttribute(serverMBeanName, "deployableFiles"));

                ObjectName deploymentPlanMBean = new ObjectName(
                    config.getDomainName() + ":type=deployment,name=deploymentPlan");
                if (!mbsc.queryMBeans(deploymentPlanMBean, null).isEmpty())
                {
                    String[] deploymentPlans = (String[])
                        mbsc.getAttribute(deploymentPlanMBean, "DeploymentPlans");
                    if (deploymentPlans != null)
                    {
                        for (String deploymentPlan : deploymentPlans)
                        {
                            // Bug JONAS-713: The DeploymentPlan MBean returns file names with
                            // unescaped URL forms, for example: /C:/Documents%20and%20Settings/...
                            //
                            // TODO: URLDecoder.decode(String, Charset) was introduced in Java 10,
                            //       simplify the below code when Codehaus Cargo is on Java 10+
                            deploymentPlan =
                                URLDecoder.decode(deploymentPlan, StandardCharsets.UTF_8.name());
                            remoteFiles.add(deploymentPlan);
                        }
                    }
                }

                for (String remoteFile : remoteFiles)
                {
                    // CARGO-1178: Be careful with Windows paths
                    if (remoteFile.replace('\\', '/').endsWith(lookForFile))
                    {
                        result = remoteFile;
                        break;
                    }
                }

                if (result == null && Boolean.parseBoolean(configuration.getPropertyValue(
                    JonasPropertySet.JONAS_UNDEPLOY_IGNORE_VERSION)))
                {
                    StringBuilder localFileNameBuilder = new StringBuilder();
                    String extension = lookForFile.substring(lookForFile.length() - 4);

                    String[] elements = localFileName.split("-");
                    for (String element : elements)
                    {
                        localFileNameBuilder.append(element);
                        if (localFileNameBuilder.length() > 0)
                        {
                            // Only look for deployables in JONAS_BASE/deploy with the same prefix
                            // and the same extension
                            for (String remoteFile : remoteFiles)
                            {
                                if (remoteFile.contains("/deploy/" + localFileNameBuilder)
                                    && remoteFile.endsWith(extension))
                                {
                                    result = remoteFile;
                                }
                            }
                        }
                    }

                    if (result != null)
                    {
                        getLogger().info("Could not find the deployable with the exact name ["
                            + localFileName + "] and "
                            + JonasPropertySet.JONAS_UNDEPLOY_IGNORE_VERSION
                            + " is set to true. Action will be done on the deployable: " + result,
                            this.getClass().getName());
                    }
                }
            }
            catch (Exception e)
            {
                throw new ContainerException("Failed looking for deployable" + deployable, e);
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
     */
    @Override
    protected String getRemoteFileName(Deployable deployable, String deployableIdentifier,
        boolean askFromServer)
    {
        String result = this.findRemoteFileName(deployable, deployableIdentifier, askFromServer);
        if (result == null)
        {
            throw new ContainerException("Cannot find deployable " + deployable
                + " in the remote JONAS_BASE/deploy");
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void redeploy(Deployable deployable, DeployableMonitor monitor)
    {
        RemoteDeployerConfig config = getConfig();

        boolean shouldUndeploy;
        try
        {
            getRemoteFileName(deployable, config.getDeployableIdentifier(), true);
            shouldUndeploy = true;
        }
        catch (ContainerException e)
        {
            getLogger().info("Cannot find remote deployable " + deployable
                + ", skipping undeploy phase of redeploy", this.getClass().getName());
            shouldUndeploy = false;
        }

        if (shouldUndeploy)
        {
            undeploy(deployable, monitor);
        }

        deploy(deployable, monitor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void redeploy(Deployable deployable)
    {
        RemoteDeployerConfig config = getConfig();

        boolean shouldUndeploy;
        try
        {
            getRemoteFileName(deployable, config.getDeployableIdentifier(), true);
            shouldUndeploy = true;
        }
        catch (ContainerException e)
        {
            getLogger().info("Cannot find remote deployable " + deployable
                + ", skipping undeploy phase of redeploy", this.getClass().getName());
            shouldUndeploy = false;
        }

        if (shouldUndeploy)
        {
            undeploy(deployable);
        }

        deploy(deployable);
    }
}
