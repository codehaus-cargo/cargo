/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
 */
public class DefaultPackagerFactory extends AbstractIntrospectionGenericHintFactory<Packager>
    implements PackagerFactory
{
    /**
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
     *            {@link AbstractFactoryRegistry#register(ClassLoader, PackagerFactory)} for the
     *            details of what this value means.
     */
    public DefaultPackagerFactory(ClassLoader classLoader)
    {
        super();

        AbstractFactoryRegistry.register(classLoader, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerPackager(String containerId, PackagerType packagerType,
        Class<? extends Packager> packagerClass)
    {
        registerImplementation(new RegistrationKey(new SimpleContainerIdentity(containerId),
            packagerType.getType()), packagerClass);
    }

    /**
     * Registers a packager using a class specified as a String.
     * 
     * @param containerId Container id.
     * @param packagerType Packager type.
     * @param packagerClassName Packager implementation class to register as a String
     */
    public void registerPackager(String containerId, PackagerType packagerType,
        String packagerClassName)
    {
        registerImplementation(new RegistrationKey(new SimpleContainerIdentity(containerId),
            packagerType.getType()), packagerClassName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPackagerRegistered(String containerId, PackagerType packagerType)
    {
        return hasMapping(new RegistrationKey(new SimpleContainerIdentity(containerId),
            packagerType.getType()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Packager createPackager(String containerId, PackagerType packagerType,
        String outputLocation)
    {
        PackagerFactoryParameters parameters = new PackagerFactoryParameters();
        parameters.outputLocation = outputLocation;

        return createImplementation(new RegistrationKey(new SimpleContainerIdentity(containerId),
            packagerType.getType()), parameters, "packager");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Constructor<? extends Packager> getConstructor(
        Class<? extends Packager> deployerClass, String hint, GenericParameters parameters)
        throws NoSuchMethodException
    {
        Constructor<? extends Packager> constructor;

        PackagerType type = PackagerType.toType(hint);

        if (type == PackagerType.DIRECTORY)
        {
            constructor = deployerClass.getConstructor(String.class);
        }
        else
        {
            throw new ContainerException("Unknown packager type [" + type.getType() + "]");
        }

        return constructor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Packager createInstance(Constructor<? extends Packager> constructor, String hint,
        GenericParameters parameters) throws Exception
    {
        String outputLocation = ((PackagerFactoryParameters) parameters).outputLocation;
        return constructor.newInstance(outputLocation);
    }
}
