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
package org.codehaus.cargo.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.discovery.jdk.JDKHooks;
import org.apache.commons.discovery.resource.ClassLoaders;
import org.apache.commons.discovery.tools.SPInterface;
import org.apache.commons.discovery.tools.Service;
import org.codehaus.cargo.container.internal.util.ResourceUtils;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;

/**
 * SPI to be implemented by container implementation to register implementations to their factories.
 * 
 * <p>
 * This class also provides static methods (to be used primarily within Cargo but can be also called
 * directly by client apps) to discover all the implementations and register them to factories.
 * 
 * <p>
 * Client apps should normally use {@code DefaultXXXFactory} classes, like
 * {@link org.codehaus.cargo.generic.deployable.DefaultDeployableFactory}, which internally uses the
 * discovery mechanism
 * 
 * <p>
 * Container implementors should override the 1-arg {@code register} methods to register its
 * implementations to the given factory.
 * 
 * @version $Id$
 */
public abstract class AbstractFactoryRegistry
{
    /**
     * Discovers all the {@link org.codehaus.cargo.container.deployable.Deployable}s and adds them
     * to the given {@link DeployableFactory}.
     * 
     * <p>
     * The discovery is done by <a href="http://java.sun.com/j2se/1.3/docs/guide/jar/jar.html">the
     * standard service loader mechanism</a>, by looking for
     * <tt>/META-INF/services/org.codehaus.cargo.generic.AbstractFactoryRegistry</tt> files.
     * 
     * @param classLoader The class loader to be used to search service provide configuration files.
     * If null, the value defaults to the thread context classloader. If that's also null, the value
     * defaults to the class loader that loaded {@link AbstractFactoryRegistry}. In the rare
     * circumstance of that also being null (which means Cargo is loaded in the bootstrap
     * classloader), the value defaults to the system class loader.
     * @param factory The factory whose {@code register} method is invoked to receive
     * {@link org.codehaus.cargo.container.deployable.Deployable}s that are discovered.
     */
    public static void register(ClassLoader classLoader, DeployableFactory factory)
    {
        for (AbstractFactoryRegistry registry : list(classLoader))
        {
            registry.register(factory);
        }
    }

    /**
     * See {@link #register(ClassLoader, DeployableFactory)} for the semantics.
     * 
     * @param classLoader See {@link #register(ClassLoader, DeployableFactory)} for the semantics.
     * @param factory See {@link #register(ClassLoader, DeployableFactory)} for the semantics.
     */
    public static void register(ClassLoader classLoader, ConfigurationFactory factory)
    {
        for (AbstractFactoryRegistry registry : list(classLoader))
        {
            registry.register(factory);
        }
    }

    /**
     * See {@link #register(ClassLoader, DeployableFactory)} for the semantics.
     * 
     * @param classLoader See {@link #register(ClassLoader, DeployableFactory)} for the semantics.
     * @param factory See {@link #register(ClassLoader, DeployableFactory)} for the semantics.
     */
    public static void register(ClassLoader classLoader, ConfigurationCapabilityFactory factory)
    {
        for (AbstractFactoryRegistry registry : list(classLoader))
        {
            registry.register(factory);
        }
    }

    /**
     * See {@link #register(ClassLoader, DeployableFactory)} for the semantics.
     * 
     * @param classLoader See {@link #register(ClassLoader, DeployableFactory)} for the semantics.
     * @param factory See {@link #register(ClassLoader, DeployableFactory)} for the semantics.
     */
    public static void register(ClassLoader classLoader, DeployerFactory factory)
    {
        for (AbstractFactoryRegistry registry : list(classLoader))
        {
            registry.register(factory);
        }
    }

    /**
     * See {@link #register(ClassLoader, DeployableFactory)} for the semantics.
     * 
     * @param classLoader See {@link #register(ClassLoader, DeployableFactory)} for the semantics.
     * @param factory See {@link #register(ClassLoader, DeployableFactory)} for the semantics.
     */
    public static void register(ClassLoader classLoader, PackagerFactory factory)
    {
        for (AbstractFactoryRegistry registry : list(classLoader))
        {
            registry.register(factory);
        }
    }

    /**
     * See {@link #register(ClassLoader, DeployableFactory)} for the semantics.
     * 
     * @param classLoader See {@link #register(ClassLoader, DeployableFactory)} for the semantics.
     * @param factory See {@link #register(ClassLoader, DeployableFactory)} for the semantics.
     */
    public static void register(ClassLoader classLoader, ContainerFactory factory)
    {
        for (AbstractFactoryRegistry registry : list(classLoader))
        {
            registry.register(factory);
        }
    }

    /**
     * See {@link #register(ClassLoader, DeployableFactory)} for the semantics.
     * 
     * @param classLoader See {@link #register(ClassLoader, DeployableFactory)} for the semantics.
     * @param factory See {@link #register(ClassLoader, DeployableFactory)} for the semantics.
     */
    public static void register(ClassLoader classLoader, ContainerCapabilityFactory factory)
    {
        for (AbstractFactoryRegistry registry : list(classLoader))
        {
            registry.register(factory);
        }
    }

    /**
     * Registers {@link org.codehaus.cargo.container.deployable.Deployable} implementations to the
     * given {@link DeployableFactory}.
     * 
     * @param factory See {@link #register(ClassLoader, DeployableFactory)}
     */
    protected abstract void register(DeployableFactory factory);

    /**
     * See {@link #register(DeployableFactory)} for the semantics.
     * 
     * @param factory See {@link #register(DeployableFactory)}
     */
    protected abstract void register(ConfigurationCapabilityFactory factory);

    /**
     * See {@link #register(DeployableFactory)} for the semantics.
     * 
     * @param factory See {@link #register(DeployableFactory)}
     */
    protected abstract void register(ConfigurationFactory factory);

    /**
     * See {@link #register(DeployableFactory)} for the semantics.
     * 
     * @param factory See {@link #register(DeployableFactory)}
     */
    protected abstract void register(DeployerFactory factory);

    /**
     * See {@link #register(DeployableFactory)} for the semantics.
     * 
     * @param factory See {@link #register(DeployableFactory)}
     */
    protected abstract void register(PackagerFactory factory);

    /**
     * See {@link #register(DeployableFactory)} for the semantics.
     * 
     * @param factory See {@link #register(DeployableFactory)}
     */
    protected abstract void register(ContainerFactory factory);

    /**
     * See {@link #register(DeployableFactory)} for the semantics.
     * 
     * @param factory See {@link #register(DeployableFactory)}
     */
    protected abstract void register(ContainerCapabilityFactory factory);

    /**
     * Lists up {@link AbstractFactoryRegistry}s that are discovered.
     * 
     * @param classLoader See {@link #register(ClassLoader, DeployableFactory)} for more details.
     * @return always non-null but can be empty.
     */
    private static List<AbstractFactoryRegistry> list(ClassLoader classLoader)
    {
        ClassLoader cl;
        ClassLoaders loaders = new ClassLoaders();

        cl = classLoader;
        if (cl != null)
        {
            loaders.put(cl);
        }

        cl = Thread.currentThread().getContextClassLoader();
        if (cl != null)
        {
            loaders.put(cl);
        }

        cl = AbstractFactoryRegistry.class.getClassLoader();
        if (cl != null)
        {
            loaders.put(cl);
        }

        cl = ResourceUtils.getResourceLoader();
        if (cl != null)
        {
            loaders.put(cl);
        }

        cl = JDKHooks.getJDKHooks().getSystemClassLoader();
        if (cl != null)
        {
            loaders.put(cl);
        }

        if (loaders.size() == 0)
        {
            // this is not our day. bail out.
            return Collections.EMPTY_LIST;
        }

        List<AbstractFactoryRegistry> registries = new ArrayList<AbstractFactoryRegistry>();
        Enumeration providers = Service.providers(
                new SPInterface(AbstractFactoryRegistry.class), loaders);
        while (providers.hasMoreElements())
        {
            Object provider = providers.nextElement();
            if (provider instanceof AbstractFactoryRegistry)
            {
                registries.add((AbstractFactoryRegistry) provider);
            }
        }

        return registries;
    }
}
