/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
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
package org.codehaus.cargo.generic.packager;

import org.codehaus.cargo.generic.spi.AbstractIntrospectionGenericHintFactory;
import org.codehaus.cargo.generic.internal.util.RegistrationKey;
import org.codehaus.cargo.generic.internal.util.SimpleContainerIdentity;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.packager.PackagerType;
import org.codehaus.cargo.container.packager.Packager;

import java.lang.reflect.Constructor;

/**
 * Default {@link PackagerFactory} implementation that has all the known container
 * packagers registered against their containers. It also supports registering new
 * packagers against any container.
 *
 * @version $Id: $
 */
public class DefaultPackagerFactory extends AbstractIntrospectionGenericHintFactory
    implements PackagerFactory
{
    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.generic.spi.AbstractGenericHintFactory.GenericParameters
     */
    private static class PackagerFactoryParameters implements GenericParameters
    {
        /**
         * The the location where the package will be generated.
         */
        public String outputLocation;
    }

    /**
     * Register default deployers.
     */
    public DefaultPackagerFactory()
    {
        super();

        // Note: Sorted by container id alphabetical order

        registerPackager("tomcat3x", PackagerType.DIRECTORY,
            "org.codehaus.cargo.container.tomcat.TomcatDirectoryPackager");
        registerPackager("tomcat4x", PackagerType.DIRECTORY,
            "org.codehaus.cargo.container.tomcat.TomcatDirectoryPackager");
        registerPackager("tomcat5x", PackagerType.DIRECTORY,
            "org.codehaus.cargo.container.tomcat.TomcatDirectoryPackager");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.generic.packager.PackagerFactory#registerPackager(String, org.codehaus.cargo.container.packager.PackagerType, Class)
     */
    public void registerPackager(String containerId, PackagerType packagerType, Class packagerClass)
    {
        registerImplementation(new RegistrationKey(new SimpleContainerIdentity(containerId),
            packagerType.getType()), packagerClass);
    }

    /**
     * Registers a packager using a class specified as a String.
     *
     * @param containerId {@inheritDoc}
     * @param packagerType {@inheritDoc}
     * @param packagerClassName the packager implementation class to register as a String
     * @see #registerPackager(String, org.codehaus.cargo.container.packager.PackagerType, Class)
     */
    public void registerPackager(String containerId, PackagerType packagerType,
        String packagerClassName)
    {
        registerImplementation(new RegistrationKey(new SimpleContainerIdentity(containerId),
            packagerType.getType()), packagerClassName);
    }

    /**
     * {@inheritDoc}
     * @see PackagerFactory#isPackagerRegistered(String, org.codehaus.cargo.container.packager.PackagerType)
     */
    public boolean isPackagerRegistered(String containerId, PackagerType packagerType)
    {
        return hasMapping(new RegistrationKey(new SimpleContainerIdentity(containerId),
            packagerType.getType()));
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.generic.packager.PackagerFactory#createPackager
     */
    public Packager createPackager(String containerId, PackagerType packagerType,
        String outputLocation)
    {
        PackagerFactoryParameters parameters = new PackagerFactoryParameters();
        parameters.outputLocation = outputLocation;

        return (Packager) createImplementation(new RegistrationKey(new SimpleContainerIdentity(
            containerId), packagerType.getType()), parameters, "packager");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.generic.spi.AbstractGenericHintFactory#getConstructor(Class, String, org.codehaus.cargo.generic.spi.AbstractGenericHintFactory.GenericParameters)
     */
    protected Constructor getConstructor(Class deployerClass, String hint,
        GenericParameters parameters) throws NoSuchMethodException
    {
        Constructor constructor;

        PackagerType type = PackagerType.toType(hint);

        if (type == PackagerType.DIRECTORY)
        {
            constructor = deployerClass.getConstructor(new Class[] {String.class});
        }
        else
        {
            throw new ContainerException("Unknown packager type [" + type.getType() + "]");
        }

        return constructor;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.generic.spi.AbstractGenericHintFactory#createInstance
     */
    protected Object createInstance(Constructor constructor, String hint,
        GenericParameters parameters) throws Exception
    {
        String outputLocation = ((PackagerFactoryParameters) parameters).outputLocation;

        return constructor.newInstance(new Object[] {outputLocation});
    }
}
