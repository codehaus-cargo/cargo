/*
 * ========================================================================
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
package org.codehaus.cargo.generic.deployer;

import java.lang.reflect.Constructor;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;
import org.codehaus.cargo.generic.internal.util.RegistrationKey;
import org.codehaus.cargo.generic.internal.util.SimpleContainerIdentity;
import org.codehaus.cargo.generic.spi.AbstractIntrospectionGenericHintFactory;

/**
 * Default {@link DeployerFactory} implementation that has all the known container deployers
 * registered against their containers. It also supports registering new deployers against any
 * container.
 * 
 * @version $Id$
 */
public class DefaultDeployerFactory extends AbstractIntrospectionGenericHintFactory<Deployer>
    implements DeployerFactory
{
    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.generic.spi.AbstractGenericHintFactory.GenericParameters
     */
    private static class DeployerFactoryParameters implements GenericParameters
    {
        /**
         * The container to deploy to.
         */
        public Container container;
    }

    /**
     * Register default deployers.
     */
    public DefaultDeployerFactory()
    {
        this(null);
    }

    /**
     * Register deployer name mappings.
     * 
     * @param classLoader ClassLoader to discover implementations from. See
     *            {@link AbstractFactoryRegistry#register(ClassLoader, DeployerFactory)} for the
     *            details of what this value means.
     */
    public DefaultDeployerFactory(ClassLoader classLoader)
    {
        super();

        AbstractFactoryRegistry.register(classLoader, this);
    }

    /**
     * {@inheritDoc}
     * 
     * @see DeployerFactory#registerDeployer(String, DeployerType, Class)
     */
    public void registerDeployer(String containerId, DeployerType deployerType,
        Class<? extends Deployer> deployerClass)
    {
        registerImplementation(new RegistrationKey(new SimpleContainerIdentity(containerId),
            deployerType.getType()), deployerClass);
    }

    /**
     * Registers a deployer using a class specified as a String.
     * 
     * @param containerId {@inheritDoc}
     * @param deployerType {@inheritDoc}
     * @param deployerClassName the deployer implementation class to register as a String
     * @see #registerDeployer(String, DeployerType, Class)
     */
    public void registerDeployer(String containerId, DeployerType deployerType,
        String deployerClassName)
    {
        registerImplementation(new RegistrationKey(new SimpleContainerIdentity(containerId),
            deployerType.getType()), deployerClassName);
    }

    /**
     * {@inheritDoc}
     * 
     * @see DeployerFactory#isDeployerRegistered(String, DeployerType)
     */
    public boolean isDeployerRegistered(String containerId, DeployerType deployerType)
    {
        return hasMapping(new RegistrationKey(new SimpleContainerIdentity(containerId),
            deployerType.getType()));
    }

    /**
     * {@inheritDoc}
     * 
     * @see DeployerFactory#getDeployerClass(String,
     *      org.codehaus.cargo.container.deployer.DeployerType)
     */
    public Class<? extends Deployer> getDeployerClass(String containerId, DeployerType deployerType)
    {
        return getMapping(new RegistrationKey(new SimpleContainerIdentity(containerId),
            deployerType.getType()));
    }

    /**
     * {@inheritDoc}
     * 
     * @see DeployerFactory#createDeployer(Container, DeployerType)
     */
    public Deployer createDeployer(Container container, DeployerType deployerType)
    {
        DeployerFactoryParameters parameters = new DeployerFactoryParameters();
        parameters.container = container;

        return createImplementation(
            new RegistrationKey(new SimpleContainerIdentity(container.getId()),
                deployerType.getType()), parameters, "deployer");
    }

    /**
     * {@inheritDoc}
     * 
     * @see DeployerFactory#createDeployer(Container)
     */
    public Deployer createDeployer(Container container)
    {
        Deployer deployer;

        DeployerType type = DeployerType.toType(container.getType());

        if (isDeployerRegistered(container.getId(), type))
        {
            getLogger().debug("Creating a default [" + type + "] deployer",
                this.getClass().getName());
            deployer = createDeployer(container, type);
        }
        else
        {
            throw new ContainerException("There's no registered deployer matching your "
                + "container's type of [" + container.getType().getType() + "]");
        }

        return deployer;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.generic.spi.AbstractGenericHintFactory#getConstructor(Class, String,
     *      GenericParameters)
     */
    @Override
    protected Constructor<? extends Deployer> getConstructor(
        Class<? extends Deployer> deployerClass, String hint, GenericParameters parameters)
        throws NoSuchMethodException
    {
        Constructor<? extends Deployer> constructor;

        DeployerType type = DeployerType.toType(hint);

        if (type == DeployerType.INSTALLED)
        {
            constructor =
                deployerClass.getConstructor(new Class[] {InstalledLocalContainer.class});
        }
        else if (type == DeployerType.EMBEDDED)
        {
            constructor =
                deployerClass.getConstructor(new Class[] {EmbeddedLocalContainer.class});
        }
        else if (type == DeployerType.REMOTE)
        {
            constructor = deployerClass.getConstructor(new Class[] {RemoteContainer.class});
        }
        else
        {
            throw new ContainerException("Unknown deployer type [" + type.getType() + "]");
        }

        return constructor;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.generic.spi.AbstractGenericHintFactory#createInstance
     */
    @Override
    protected Deployer createInstance(Constructor<? extends Deployer> constructor, String hint,
        GenericParameters parameters) throws Exception
    {
        Container container = ((DeployerFactoryParameters) parameters).container;

        return constructor.newInstance(new Object[] {container});
    }
}
