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
package org.codehaus.cargo.generic.packager;

import java.lang.reflect.Constructor;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.packager.Packager;
import org.codehaus.cargo.container.packager.PackagerType;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;
import org.codehaus.cargo.generic.internal.util.RegistrationKey;
import org.codehaus.cargo.generic.internal.util.SimpleContainerIdentity;
import org.codehaus.cargo.generic.spi.AbstractIntrospectionGenericHintFactory;

/**
 * Default {@link PackagerFactory} implementation that has all the known container packagers
 * registered against their containers. It also supports registering new packagers against any
 * container.
 * 
 * @version $Id$
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
        this(null);
    }

    /**
     * Register packager name mappings.
     * 
     * @param classLoader ClassLoader to discover implementations from. See
     * {@link AbstractFactoryRegistry#register(ClassLoader, PackagerFactory)} for the details of
     * what this value means.
     */
    public DefaultPackagerFactory(ClassLoader classLoader)
    {
        super();

        AbstractFactoryRegistry.register(classLoader, this);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.generic.packager.PackagerFactory#registerPackager(String,
     * org.codehaus.cargo.container.packager.PackagerType, Class)
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
     * @see PackagerFactory#isPackagerRegistered(String,
     * org.codehaus.cargo.container.packager.PackagerType)
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
     * @see org.codehaus.cargo.generic.spi.AbstractGenericHintFactory#getConstructor(Class, String,
     * org.codehaus.cargo.generic.spi.AbstractGenericHintFactory.GenericParameters)
     */
    @Override
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
    @Override
    protected Object createInstance(Constructor constructor, String hint,
        GenericParameters parameters) throws Exception
    {
        String outputLocation = ((PackagerFactoryParameters) parameters).outputLocation;

        return constructor.newInstance(new Object[] {outputLocation});
    }
}
