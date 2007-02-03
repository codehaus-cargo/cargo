/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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
package org.codehaus.cargo.generic.deployable;

import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.EJB;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.generic.spi.AbstractIntrospectionGenericHintFactory;
import org.codehaus.cargo.generic.internal.util.RegistrationKey;
import org.codehaus.cargo.generic.internal.util.SimpleContainerIdentity;

import java.lang.reflect.Constructor;

/**
 * Default deployable factory that returns deployables for a given container. The reason deployable
 * can be different for different containers is because for some container Cargo understand
 * container-specific files. For example for Tomcat Cargo understand the context.xml file.
 *  
 * @version $Id$
 */
public class DefaultDeployableFactory extends AbstractIntrospectionGenericHintFactory 
    implements DeployableFactory
{
    /**
     * Default container id under which we register the default deployables (EAR, WAR, EJB). This
     * is to prevent having to register the default deployables against each and every container.
     */
    private static final String DEFAULT_CONTAINER_ID = "default";

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.generic.spi.AbstractGenericHintFactory.GenericParameters
     */
    private static class DeployableFactoryParameters implements GenericParameters
    {
        /**
         * The deployable file.
         */
        public String deployable;
    }
    
    /**
     * Register deployable classes mappings.
     */
    public DefaultDeployableFactory()
    {
        // The WAR, EJB and EAR deployables are registered by default against all containers.
        // In order not to have to individually register against each container id we
        // create a fictitious default container id.
        registerDeployable(DEFAULT_CONTAINER_ID, DeployableType.WAR, WAR.class);
        registerDeployable(DEFAULT_CONTAINER_ID, DeployableType.EJB, EJB.class);
        registerDeployable(DEFAULT_CONTAINER_ID, DeployableType.EAR, EAR.class);
        
        // Register container-specific mappings

        registerDeployable("tomcat5x", DeployableType.WAR,
            "org.codehaus.cargo.container.tomcat.TomcatWAR");

        registerDeployable("geronimo1x", DeployableType.WAR,
            "org.codehaus.cargo.container.geronimo.deployable.GeronimoWAR");
        registerDeployable("geronimo1x", DeployableType.EJB,
            "org.codehaus.cargo.container.geronimo.deployable.GeronimoEJB");
        registerDeployable("geronimo1x", DeployableType.EAR,
            "org.codehaus.cargo.container.geronimo.deployable.GeronimoEAR");

        // TODO: Register JBossWAR here when we add JBoss support
    }

    /**
     * {@inheritDoc}
     * @see DeployableFactory#registerDeployable(String, DeployableType, Class)
     */
    public void registerDeployable(String containerId, DeployableType deployableType,
        Class deployableClass)
    {
        registerImplementation(new RegistrationKey(new SimpleContainerIdentity(containerId),
            deployableType.getType()), deployableClass);
    }

    /**
     * Registers a deployable using a class specified as a String.
     *
     * @param containerId {@inheritDoc}
     * @param deployableType {@inheritDoc}
     * @param deployableClassName the deployable implementation class to register as a String
     * @see #registerDeployable(String, DeployableType, Class)
     */
    public void registerDeployable(String containerId, DeployableType deployableType,
        String deployableClassName)
    {
        registerImplementation(new RegistrationKey(new SimpleContainerIdentity(containerId),
            deployableType.getType()), deployableClassName);
    }
    
    /**
     * {@inheritDoc}
     * @see DeployableFactory#isDeployableRegistered
     */
    public boolean isDeployableRegistered(String containerId, DeployableType deployableType)
    {
        return hasMapping(new RegistrationKey(new SimpleContainerIdentity(containerId),
            deployableType.getType()));
    }

    /**
     * {@inheritDoc}
     * @see DeployableFactory#createDeployable
     */
    public Deployable createDeployable(String containerId, String deployableLocation,
        DeployableType deployableType)
    {
        Deployable deployable;

        DeployableFactoryParameters parameters = new DeployableFactoryParameters();
        parameters.deployable = deployableLocation;
        
        // First, try to locate a container-specific deployable mapping
        if (isDeployableRegistered(containerId, deployableType))
        {
            deployable = (Deployable) createImplementation(new RegistrationKey(
                new SimpleContainerIdentity(containerId), deployableType.getType()),
                parameters, "deployable");
        }
        else
        {
            // Use a default deployable
            deployable = (Deployable) createImplementation(new RegistrationKey(
                new SimpleContainerIdentity(DEFAULT_CONTAINER_ID), deployableType.getType()),
                parameters, "deployable");
        }
        
        return deployable;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.generic.spi.AbstractGenericHintFactory#getConstructor
     */
    protected Constructor getConstructor(Class deployableClass, String hint,
        GenericParameters parameters) throws NoSuchMethodException
    {
        return deployableClass.getConstructor(new Class[] {String.class});
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.generic.spi.AbstractGenericHintFactory#createInstance
     */
    protected Object createInstance(Constructor constructor, String hint,
        GenericParameters parameters) throws Exception
    {
        String deployable = ((DeployableFactoryParameters) parameters).deployable;
        return constructor.newInstance(new Object[] {deployable});
    }
}
