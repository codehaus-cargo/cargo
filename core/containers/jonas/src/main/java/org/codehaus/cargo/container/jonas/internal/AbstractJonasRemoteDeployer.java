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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.jonas.JonasPropertySet;
import org.codehaus.cargo.container.spi.deployer.AbstractRemoteDeployer;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;

/**
 * Abstract base class for JOnAS remote deployment.
 * 
 * @version $Id$
 */
public abstract class AbstractJonasRemoteDeployer extends AbstractRemoteDeployer
{
    /**
     * The run time configuration.
     */
    protected RuntimeConfiguration configuration;

    /**
     * Action types.
     */
    protected static final class ActionType
    {
        /**
         * The "deploy" action.
         */
        public static final ActionType DEPLOY = new ActionType("deploy");

        /**
         * The "upload and deploy" action.
         */
        public static final ActionType UPLOAD_DEPLOY = new ActionType("uploadDeploy");

        /**
         * The "undeploy" action.
         */
        public static final ActionType UNDEPLOY = new ActionType("undeploy");

        /**
         * A unique id that identifies the action.
         */
        private String type;

        /**
         * @param type A unique id that identifies the action.
         */
        private ActionType(String type)
        {
            this.type = type;
        }

        /**
         * {@inheritDoc}
         * 
         * @see Object#equals(Object)
         */
        @Override
        public boolean equals(Object object)
        {
            boolean result = false;
            if (object instanceof ActionType)
            {
                ActionType type = (ActionType) object;
                if (type.type.equalsIgnoreCase(this.type))
                {
                    result = true;
                }
            }
            return result;
        }

        /**
         * {@inheritDoc}
         * 
         * @see Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            return this.type.hashCode();
        }

        /**
         * @return the deployable type
         */
        public String getType()
        {
            return this.type;
        }

        /**
         * {@inheritDoc}
         * 
         * @see Object#toString()
         */
        @Override
        public String toString()
        {
            return this.type;
        }
    }

    /**
     * Target types.
     */
    protected static final class TargetType
    {
        /**
         * One server.
         */
        public static final TargetType SERVER = new TargetType("server");

        /**
         * The domain master.
         */
        public static final TargetType DOMAIN = new TargetType("domain");

        /**
         * A unique id that identifies the target.
         */
        private String type;

        /**
         * @param type A unique id that identifies the target.
         */
        private TargetType(String type)
        {
            this.type = type;
        }

        /**
         * {@inheritDoc}
         * 
         * @see Object#equals(Object)
         */
        @Override
        public boolean equals(Object object)
        {
            boolean result = false;
            if (object instanceof TargetType)
            {
                TargetType type = (TargetType) object;
                if (type.type.equalsIgnoreCase(this.type))
                {
                    result = true;
                }
            }
            return result;
        }

        /**
         * {@inheritDoc}
         * 
         * @see Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            return this.type.hashCode();
        }

        /**
         * @return the deployable type
         */
        public String getType()
        {
            return this.type;
        }

        /**
         * {@inheritDoc}
         * 
         * @see Object#toString()
         */
        @Override
        public String toString()
        {
            return this.type;
        }
    }

    /**
     * Constructor.
     * 
     * @param container the remote container
     */
    public AbstractJonasRemoteDeployer(RemoteContainer container)
    {
        super(container);

        configuration = container.getConfiguration();
    }

    /**
     * Get the MBean Connection factory.
     * 
     * @return the MBean Connection factory
     */
    public abstract MBeanServerConnectionFactory getMBeanServerConnectionFactory();

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.deployer.Deployer#deploy(Deployable)
     */
    @Override
    public void deploy(Deployable deployable)
    {
        if (deployable.getType() == DeployableType.WAR)
        {
            WAR war = (WAR) deployable;
            if (war.isExpanded())
            {
                getLogger().warn("Remote deployer does not support expanded WAR deployment",
                    this.getClass().getName());
                return;
            }
        }
        RemoteDeployerConfig config = getConfig();
        MBeanServerConnectionFactory factory = null;
        try
        {
            factory = getMBeanServerConnectionFactory();
            MBeanServerConnection mbsc = factory.getServerConnection(configuration);

            ObjectName serverMBeanName = getServerMBeanName(config.getDomainName(), config
                .getServerName());

            String filePathOnServer = uploadDeployableOnServer(deployable, mbsc, serverMBeanName,
                config);

            if (config.getClusterName() == null)
            {
                String operationName = getOperationName(ActionType.DEPLOY, deployable.getType(),
                    TargetType.SERVER);
                getLogger().debug("Calling deployment operation " + operationName + " on server",
                    getClass().getName());
                mbsc.invoke(serverMBeanName, operationName, new Object[]
                {
                    filePathOnServer
                }, new String[]
                {
                    String.class.getName()
                });
            }
            else
            {
                String operationName = getOperationName(ActionType.UPLOAD_DEPLOY,
                    deployable.getType(), TargetType.DOMAIN);
                getLogger().debug(
                    "Calling deployment operation " + operationName + " on domain master",
                    getClass().getName());
                ObjectName domainMBeanName = getDomainMBeanName(config.getDomainName());
                String[] serverNames = (String[]) mbsc.invoke(domainMBeanName, "getServerNames",
                    new Object[]
                    {
                        config.getClusterName()
                    }, new String[]
                    {
                        String.class.getName()
                    });
                mbsc.invoke(domainMBeanName, operationName, new Object[]
                {
                    serverNames, filePathOnServer, Boolean.TRUE
                }, new String[]
                {
                    String[].class.getName(), String.class.getName(), boolean.class.getName()
                });
            }
        }
        catch (Exception ex)
        {
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

    /**
     * Returns the operation name for the given deployable and action type.
     * 
     * @param actionType Action type.
     * @param deployableType Deployable type.
     * @param targetType Target type.
     * 
     * @return Operation name.
     */
    protected abstract String getOperationName(ActionType actionType,
        DeployableType deployableType, TargetType targetType);

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.deployer.Deployer#undeploy(Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        RemoteDeployerConfig config = getConfig();
        MBeanServerConnectionFactory factory = null;
        try
        {
            factory = getMBeanServerConnectionFactory();
            MBeanServerConnection mbsc = factory.getServerConnection(configuration);

            ObjectName serverMBeanName = getServerMBeanName(config.getDomainName(), config
                .getServerName());

            String remoteFileName = getRemoteFileName(deployable, config.getDeployableIdentifier(),
                true);

            if (config.getClusterName() == null)
            {
                String operationName = getOperationName(ActionType.UNDEPLOY, deployable.getType(),
                    TargetType.SERVER);
                getLogger().debug("Calling undeployment operation " + operationName + " on server",
                    getClass().getName());
                mbsc.invoke(serverMBeanName, operationName, new Object[]
                {
                    remoteFileName
                }, new String[]
                {
                    String.class.getName()
                });

                // See bug CARGO-620
                getLogger().debug("Calling garbage collector on server", getClass().getName());
                mbsc.invoke(serverMBeanName, "runGC", null, null);
            }
            else
            {
                String operationName = getOperationName(ActionType.UNDEPLOY, deployable.getType(),
                    TargetType.DOMAIN);
                getLogger().debug(
                    "Calling undeployment operation " + operationName + " on domain master",
                    getClass().getName());
                ObjectName domainMBeanName = getDomainMBeanName(config.getDomainName());
                String[] serverNames = (String[]) mbsc.getAttribute(domainMBeanName, "serverNames");
                mbsc.invoke(domainMBeanName, operationName, new Object[]
                {
                    serverNames, remoteFileName
                }, new String[]
                {
                    String[].class.getName(), String.class.getName()
                });
            }

            // misc configuration property to avoid to remove the file on the server side.
            // used to avoid a bug with JOnAS removeModuleFile remote method call
            // who is removing a wrong file during the
            // org.codehaus.cargo.sample.java.RemoteDeploymentTest
            // unit test call
            String skipRemoval = this.configuration
                .getPropertyValue("cargo.jonas.remote.deployer.skip.module.removal");
            if (skipRemoval == null || !Boolean.valueOf(skipRemoval).booleanValue())
            {
                Boolean removed = (Boolean) mbsc.invoke(serverMBeanName, "removeModuleFile",
                    new Object[]
                    {
                        remoteFileName
                    }, new String[]
                    {
                        String.class.getName()
                    });
                if (!removed.booleanValue())
                {
                    getLogger().warn("Unable to remove remote file " + remoteFileName,
                        this.getClass().getName());
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

    /**
     * Get the remote file name.
     * 
     * @param deployable the deployable Object.
     * @param deployableIdentifier the deployable object ID.
     * @param askFromServer whether to ask from server (in order to have a full path).
     * @return the remote file Name.
     */
    protected abstract String getRemoteFileName(Deployable deployable, String deployableIdentifier,
        boolean askFromServer);

    /**
     * Get the server MBean.
     * 
     * @param domainName domain Name
     * @param serverName Server Name
     * @return the server Mbean Name
     * @throws MalformedObjectNameException throwing when object name is wrong
     */
    protected ObjectName getServerMBeanName(String domainName, String serverName)
        throws MalformedObjectNameException
    {
        if (domainName == null || domainName.trim().length() == 0)
        {
            throw new MalformedObjectNameException("Empty domain name provided");
        }

        if (serverName == null || serverName.trim().length() == 0)
        {
            throw new MalformedObjectNameException("Empty server name provided");
        }

        return new ObjectName(domainName + ":j2eeType=J2EEServer,name=" + serverName);
    }

    /**
     * Get the Domain MBean.
     * 
     * @param domainName domain Name
     * @return the server Mbean Name
     * @throws MalformedObjectNameException throwing when object name is wrong
     */
    protected ObjectName getDomainMBeanName(String domainName) throws MalformedObjectNameException
    {
        if (domainName == null || domainName.trim().length() == 0)
        {
            throw new MalformedObjectNameException("Empty domain name provided");
        }

        return new ObjectName(domainName + ":j2eeType=J2EEDomain,name=" + domainName);
    }

    /**
     * Get the Deployer configuration.
     * 
     * @return the Deployer configuration
     */
    protected RemoteDeployerConfig getConfig()
    {
        RemoteDeployerConfig config = new RemoteDeployerConfig();
        config.setDeployableIdentifier(this.configuration
            .getPropertyValue(JonasPropertySet.JONAS_DEPLOYABLE_IDENTIFIER));
        config.setServerName(this.configuration
            .getPropertyValue(JonasPropertySet.JONAS_SERVER_NAME));
        config.setDomainName(this.configuration
            .getPropertyValue(JonasPropertySet.JONAS_DOMAIN_NAME));
        config.setClusterName(this.configuration
            .getPropertyValue(JonasPropertySet.JONAS_CLUSTER_NAME));

        return config;
    }

    /**
     * Upload the deploybale on the server.
     * 
     * @param deployable he deployable Object
     * @param mbsc MBean Server Connection
     * @param serverMBeanName he deployable Object
     * @param config the deployer configuration
     * @return the file Path On the Server
     * @throws InstanceNotFoundException Instance Not Found Exception
     * @throws MBeanException MBean Exception
     * @throws ReflectionException Reflection Exception
     * @throws IOException IO Exception
     */
    private String uploadDeployableOnServer(Deployable deployable, MBeanServerConnection mbsc,
        ObjectName serverMBeanName, RemoteDeployerConfig config) throws InstanceNotFoundException,
        MBeanException, ReflectionException, IOException
    {
        getLogger().debug("Uploading file \"" + deployable.getFile() + "\" on server",
            this.getClass().getName());

        // Read file
        File file = new File(deployable.getFile());
        FileHandler fileHandler = new DefaultFileHandler();
        FileInputStream in = new FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        fileHandler.copy(in, out);
        in.close();

        // Send file
        String remoteFileName = getRemoteFileName(deployable, config.getDeployableIdentifier(),
            false);
        String filePathOnServer = (String) mbsc.invoke(serverMBeanName, "sendFile", new Object[]
        {
            out.toByteArray(), remoteFileName, Boolean.TRUE
        }, new String[]
        {
            byte[].class.getName(), String.class.getName(), boolean.class.getName()
        });

        // Check if file has been sent correctly
        if (filePathOnServer == null || filePathOnServer.trim().length() == 0)
        {
            throw new ContainerException("Server returned a null uploaded file path");
        }

        getLogger().debug("File uploaded on server, saved as \"" + filePathOnServer + "\"",
            this.getClass().getName());
        return filePathOnServer;
    }

    /**
     * This class represents the Remote Deployer Configuration.
     */
    protected class RemoteDeployerConfig
    {
        /**
         * Deployable identifier Id.
         */
        private String deployableIdentifier;

        /**
         * Server Name.
         */
        private String serverName;

        /**
         * Domain Name.
         */
        private String domainName;

        /**
         * Cluster name if deployment is to be done on a domain.
         */
        private String clusterName;

        /**
         * @return the Deployable's identifier.
         */
        public String getDeployableIdentifier()
        {
            return deployableIdentifier;
        }

        /**
         * @param deployableIdentifier Deployable's identifier.
         */
        public void setDeployableIdentifier(String deployableIdentifier)
        {
            this.deployableIdentifier = deployableIdentifier;
        }

        /**
         * @return the server name.
         */
        public String getServerName()
        {
            return serverName;
        }

        /**
         * @param serverName the server name.
         */
        public void setServerName(String serverName)
        {
            this.serverName = serverName;
        }

        /**
         * @return the domain name.
         */
        public String getDomainName()
        {
            return domainName;
        }

        /**
         * @param domainName the domain name.
         */
        public void setDomainName(String domainName)
        {
            this.domainName = domainName;
        }

        /**
         * @return the cluster name if deployment is to be done on a domain.
         */
        public String getClusterName()
        {
            return clusterName;
        }

        /**
         * 
         * @param clusterName the cluster name if deployment is to be done on a domain, null
         * otherwise.
         */
        public void setClusterName(String clusterName)
        {
            this.clusterName = clusterName;
        }
    }
}
