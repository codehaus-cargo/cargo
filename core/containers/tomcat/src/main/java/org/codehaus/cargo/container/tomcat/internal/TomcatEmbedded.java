/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
package org.codehaus.cargo.container.tomcat.internal;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.container.ContainerException;

/**
 * Wrapper classes around Tomcat embedded API to hide reflection.
 */
public final class TomcatEmbedded
{
    /** reflection method. */
    private Constructor embeddedNew;

    /** reflection method. */
    private Method embeddedCreateEngine;

    /** reflection method. */
    private Method embeddedCreateHost;

    /** reflection method. */
    private Method embeddedStart;

    /** reflection method. */
    private Method embeddedStop;

    /** reflection method. */
    private Method embeddedAddEngine;

    /** reflection method. */
    private Method embeddedCreateConnector;

    /** reflection method. */
    private Method embeddedAddConnector;

    /** reflection method. */
    private Method embeddedCreateContext;

    /** reflection method. */
    private Method embeddedSetRealm;

    /** reflection method. */
    private Method embeddedSetCatalinaBase;

    /** reflection method. */
    private Method embeddedSetCatalinaHome;

    /** reflection method. */
    private Method embeddedSetPort;

    /** reflection method. */
    private Method embeddedEnableNaming;

    /** reflection method. */
    private Method embeddedGetConnector;

    /** reflection method. */
    private Method embeddedGetEngine;

    /** reflection method. */
    private Method embeddedGetHost;

    /** reflection method. */
    private Method engineSetName;

    /** reflection method. */
    private Method engineAddChild;

    /** reflection method. */
    private Method engineSetDefaultHost;

    /** reflection method. */
    private Method engineSetParentClassLoader;

    /** reflection method. */
    private Method engineSetService;

    /** reflection method. */
    private Method engineSetRealm;

    /** reflection method. */
    private Method standardEngineSetBaseDir;

    /** reflection method. */
    private Method connectorDestroy;

    /** context class. */
    private Class contextClass;

    /** reflection method. */
    private Method contextDestroy;

    /** reflection method. */
    private Method contextReload;

    /** reflection method. */
    private Method contextSetAvailable;

    /** reflection method. */
    private Method contextStart;

    /** reflection method. */
    private Method contextStop;

    /** reflection method. */
    private Method contextAddParameter;

    /** reflection method. */
    private Method hostSetAutoDeploy;

    /** reflection method. */
    private Method hostGetName;

    /** reflection method. */
    private Method hostAddChild;

    /** reflection method. */
    private Method hostFindChild;

    /** reflection method. */
    private Method hostFindChildren;

    /** reflection method. */
    private Method hostRemoveChild;

    /** reflection method. */
    private Constructor memoryRealmNew;

    /** reflection method. */
    private Method memoryRealmSetPathname;

    /**
     * ClassLoader to load Tomcat.
     */
    private final ClassLoader classLoader;

    /**
     * Prepares the reflection access to Tomcat.
     * 
     * @param classLoader the class loader used to load Tomcat classes. Can be null.
     * @throws Exception if an error happens when creating the Tomcat objects by reflection
     */
    public TomcatEmbedded(ClassLoader classLoader) throws Exception
    {
        this.classLoader = classLoader;
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try
        {
            // Tomcat uses commons-logging, which tries to use thread context loader
            // for loading resources. We need that to resolve to classes inside
            // Tomcat. See http://www.qos.ch/logging/classloader.jsp
            Thread.currentThread().setContextClassLoader(classLoader);

            preloadEngine(classLoader);
            preloadMemoryRealm(classLoader);
            preloadEmbedded(classLoader);
            preloadContext(classLoader);
            preloadHost(classLoader);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Preload the methods we'll need from the <code>org.apache.catalina.Engine</code> class.
     * 
     * @param classLoader Tomcat classes will be loaded from this class loader. Can be null.
     * @throws Exception If reflection fails.
     */
    private void preloadEngine(ClassLoader classLoader) throws Exception
    {
        Class engine = Class.forName("org.apache.catalina.Engine", true, classLoader);
        Class container = Class.forName("org.apache.catalina.Container", true, classLoader);

        engineSetName = engine.getMethod("setName", String.class);
        engineAddChild = engine.getMethod("addChild", container);
        engineSetDefaultHost = engine.getMethod("setDefaultHost", String.class);
        engineSetParentClassLoader = engine.getMethod("setParentClassLoader", ClassLoader.class);

        Class service = Class.forName("org.apache.catalina.Service", true, classLoader);
        engineSetService = engine.getMethod("setService", service);

        Class standardEngine =
            Class.forName("org.apache.catalina.core.StandardEngine", true, classLoader);
        try
        {
            standardEngineSetBaseDir = standardEngine.getMethod("setBaseDir", String.class);
        }
        catch (NoSuchMethodException ignored)
        {
            // This is Tomcat 8.x or newer
        }
    }

    /**
     * Preload the methods we'll need from the <code>org.apache.catalina.realm.MemoryRealm</code>
     * class.
     * 
     * @param classLoader Tomcat classes will be loaded from this class loader. Can be null.
     * @throws Exception If reflection fails.
     */
    private void preloadMemoryRealm(ClassLoader classLoader) throws Exception
    {
        Class memoryRealm =
            Class.forName("org.apache.catalina.realm.MemoryRealm", true, classLoader);
        memoryRealmNew = memoryRealm.getConstructor();
        memoryRealmSetPathname = memoryRealm.getMethod("setPathname", String.class);
    }

    /**
     * Preload the methods we'll need from the <code>org.apache.catalina.Context</code> class.
     * 
     * @param classLoader Tomcat classes will be loaded from this class loader. Can be null.
     * @throws Exception If reflection fails.
     */
    private void preloadContext(ClassLoader classLoader) throws Exception
    {
        contextClass = Class.forName("org.apache.catalina.Context", true, classLoader);
        try
        {
            contextDestroy = contextClass.getMethod("destroy");
        }
        catch (NoSuchMethodException ignored)
        {
            // Context.destroy only exists since Tomcat 7.x
        }
        contextReload = contextClass.getMethod("reload");
        try
        {
            contextSetAvailable =
                contextClass.getMethod("setAvailable", boolean.class);
        }
        catch (NoSuchMethodException e)
        {
            contextStart = contextClass.getMethod("start");
            contextStop = contextClass.getMethod("stop");
        }
        contextAddParameter =
            contextClass.getMethod("addParameter", String.class, String.class);
    }

    /**
     * Preload the methods we'll need from the <code>org.apache.catalina.Host</code> class.
     * 
     * @param classLoader Tomcat classes will be loaded from this class loader. Can be null.
     * @throws Exception If reflection fails.
     */
    private void preloadHost(ClassLoader classLoader) throws Exception
    {
        Class container = Class.forName("org.apache.catalina.Container", true, classLoader);

        Class host = Class.forName("org.apache.catalina.Host", true, classLoader);
        hostSetAutoDeploy = host.getMethod("setAutoDeploy", boolean.class);
        hostGetName = host.getMethod("getName");
        hostAddChild = host.getMethod("addChild", container);
        hostFindChild = host.getMethod("findChild", String.class);
        hostFindChildren = host.getMethod("findChildren");
        hostRemoveChild = host.getMethod("removeChild", container);
    }

    /**
     * Preload the methods we'll need from the <code>org.apache.catalina.startup.Embedded</code>
     * class.
     * 
     * @param classLoader Tomcat classes will be loaded from this class loader. Can be null.
     * @throws Exception If reflection fails.
     */
    private void preloadEmbedded(ClassLoader classLoader) throws Exception
    {
        Class realm = Class.forName("org.apache.catalina.Realm", true, classLoader);
        Class connector;
        try
        {
            // this works for Tomcat 5.0.x
            connector = Class.forName("org.apache.catalina.Connector", true, classLoader);
        }
        catch (ClassNotFoundException e)
        {
            // and this for Tomcat 5.5.x and newer
            connector = Class.forName("org.apache.catalina.connector.Connector", true, classLoader);
        }
        Class engine = Class.forName("org.apache.catalina.Engine", true, classLoader);

        Class embedded;
        try
        {
            embedded = Class.forName("org.apache.catalina.startup.Embedded", true, classLoader);
            embeddedCreateEngine = embedded.getMethod("createEngine");
            embeddedCreateHost =
                embedded.getMethod("createHost", String.class, String.class);
            embeddedAddEngine = embedded.getMethod("addEngine", engine);
            embeddedCreateConnector = embedded
                .getMethod("createConnector", InetAddress.class, int.class, boolean.class);
            embeddedAddConnector = embedded.getMethod("addConnector", connector);
            embeddedCreateContext =
                embedded.getMethod("createContext", String.class, String.class);
            embeddedSetRealm = embedded.getMethod("setRealm", realm);
            embeddedSetCatalinaBase =
                embedded.getMethod("setCatalinaBase", String.class);
            embeddedSetCatalinaHome =
                embedded.getMethod("setCatalinaHome", String.class);
        }
        catch (ClassNotFoundException e)
        {
            // Tomcat 8.x and newer don't have org.apache.catalina.startup.Embedded anymore
            embedded = Class.forName("org.apache.catalina.startup.Tomcat", true, classLoader);
            embeddedSetCatalinaBase = embedded.getMethod("setBaseDir", String.class);
            embeddedSetPort = embedded.getMethod("setPort", int.class);
            embeddedEnableNaming = embedded.getMethod("enableNaming");
            embeddedGetConnector = embedded.getMethod("getConnector");
            embeddedGetEngine = embedded.getMethod("getEngine");
            embeddedGetHost = embedded.getMethod("getHost");
            engineSetRealm = engine.getMethod("setRealm", realm);
            embeddedCreateContext =
                embedded.getMethod("addWebapp", String.class, String.class);
        }

        try
        {
            // See Tomcat8xEmbeddedLocalContainer#getClassLoader() to understand why we do this
            Class tomcatURLStreamHandlerFactory =
                Class.forName("org.apache.catalina.webresources.TomcatURLStreamHandlerFactory",
                    true, classLoader);
            Method getInstance =
                tomcatURLStreamHandlerFactory.getMethod("getInstance");
            getInstance.invoke(null);
        }
        catch (ClassNotFoundException e)
        {
            // Tomcat 11.x and newer don't have TomcatURLStreamHandlerFactory anymore
        }
        embeddedNew = embedded.getConstructor();
        embeddedStart = embedded.getMethod("start");
        embeddedStop = embedded.getMethod("stop");
        connectorDestroy = connector.getMethod("destroy");
    }

    /**
     * Wraps an object and invokes methods through reflection.
     */
    private class Wrapper
    {
        /**
         * Wrapped object to be accessed via reflection.
         */
        protected final Object core;

        /**
         * @param core the wrapped object to be accessed via reflection.
         */
        public Wrapper(Object core)
        {
            if (core == null)
            {
                throw new NullPointerException("Passed core is null");
            }
            this.core = core;
        }

        /**
         * Invokes a method on the wrapped object.
         * 
         * @param method the method to invoke
         * @return the value from the invocation.
         */
        protected Object invoke(Method method)
        {
            return invoke(method, new Object[0]);
        }

        /**
         * Invokes a method on the wrapped object.
         * 
         * @param method the method to invoke
         * @param arg1 the 1st argument for invocations.
         * @return the value from the invocation.
         */
        protected Object invoke(Method method, Object arg1)
        {
            return invoke(method, new Object[] {arg1});
        }

        /**
         * Invokes a method on the wrapped object.
         * 
         * @param method the method to invoke
         * @param arg1 the 1st argument for invocations.
         * @param arg2 the 2nd argument for invocations.
         * @return the value from the invocation.
         */
        protected Object invoke(Method method, Object arg1, Object arg2)
        {
            return invoke(method, new Object[] {arg1, arg2});
        }

        /**
         * Invokes a method on the wrapped object.
         * 
         * @param method the method to invoke
         * @param arg1 the 1st argument for invocations.
         * @param arg2 the 2nd argument for invocations.
         * @param arg3 the 3rd argument for invocations.
         * @return the value from the invocation.
         */
        protected Object invoke(Method method, Object arg1, Object arg2, Object arg3)
        {
            return invoke(method, new Object[] {arg1, arg2, arg3});
        }

        /**
         * Invokes a method on the wrapped object.
         * 
         * @param method the method to invoke
         * @param args the arguments for invocations.
         * @return the value from the invocation.
         */
        protected Object invoke(Method method, Object[] args)
        {
            // unwrap everything
            for (int i = 0; i < args.length; i++)
            {
                if (args[i] instanceof Wrapper)
                {
                    args[i] = ((Wrapper) args[i]).core;
                }
            }

            // why do we set context class loader? see the comment inside the constructor
            // about commons logging.
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(TomcatEmbedded.this.classLoader);
            try
            {
                return method.invoke(core, args);
            }
            catch (IllegalAccessException | InvocationTargetException e)
            {
                throw new ContainerException("Tomcat reported an error: " + e.getMessage(), e);
            }
            finally
            {
                Thread.currentThread().setContextClassLoader(cl);
            }
        }
    }

    /**
     * Copy of <code>org.apache.catalina.Context</code>
     */
    public final class Context extends Wrapper
    {
        /**
         * Wraps <code>org.apache.catalina.Context</code> object.
         * 
         * @param context object to be wrapped.
         */
        public Context(Object context)
        {
            super(context);
        }

        /**
         * Destroys the context.
         */
        public void destroy()
        {
            invoke(contextDestroy);
        }

        /**
         * Reloads this web application.
         */
        public void reload()
        {
            invoke(contextReload);
        }

        /**
         * Makes web application available/unavailable.
         * 
         * @param b true to make it available.
         */
        public void setAvailable(boolean b)
        {
            if (contextSetAvailable != null)
            {
                invoke(contextSetAvailable, b);
            }
            else if (b)
            {
                invoke(contextStart);
            }
            else
            {
                invoke(contextStop);
            }
        }

        /**
         * Add a new context initialization parameter.
         * 
         * @param key non-null parameter name.
         * @param value value
         */
        public void addParameter(String key, String value)
        {
            invoke(contextAddParameter, key, value);
        }
    }

    /**
     * Copy of <code>org.apache.catalina.Host</code>
     */
    public final class Host extends Wrapper
    {
        /**
         * Wraps a {@link Host} object.
         * 
         * @param core non-null.
         */
        public Host(Object core)
        {
            super(core);
        }

        /**
         * Supposed to enable auto-deployment of war file.
         * 
         * @param b true to enable.
         */
        public void setAutoDeploy(boolean b)
        {
            invoke(hostSetAutoDeploy, b);
        }

        /**
         * Gets the name of this host.
         * 
         * @return host name.
         */
        public String getName()
        {
            return (String) invoke(hostGetName);
        }

        /**
         * Deploys a web application.
         * 
         * @param context context to be deployed.
         */
        public void addChild(Context context)
        {
            invoke(hostAddChild, context);
        }

        /**
         * Return all context children.
         * 
         * @return All context children.
         */
        public Context[] findChildren()
        {
            List<Context> contexts = new ArrayList<Context>();
            Object[] children = (Object[]) invoke(hostFindChildren);
            for (Object child : children)
            {
                if (child.getClass().isAssignableFrom(contextClass))
                {
                    contexts.add(new Context(child));
                }
            }
            Context[] contextsArray = new Context[contexts.size()];
            contextsArray = contexts.toArray(contextsArray);
            return contextsArray;
        }

        /**
         * Finds a child with the given name.
         * 
         * @param name Name of the child.
         * @return Child with the given name.
         */
        public Context findChild(String name)
        {
            return new Context(invoke(hostFindChild, name));
        }

        /**
         * Removes a web application.
         * 
         * @param context context to be removed.
         */
        public void removeChild(Context context)
        {
            invoke(hostRemoveChild, context);
        }
    }

    /**
     * Copy of <code>org.apache.catalina.startup.Embedded</code>
     */
    public final class Embedded extends Wrapper
    {
        /**
         * Creates a new {@link Embedded} Tomcat.
         */
        public Embedded()
        {
            super(newInstance(embeddedNew));
        }

        /**
         * Creates a new engine.
         * 
         * @return non-null
         */
        public Engine createEngine()
        {
            return new Engine(invoke(embeddedCreateEngine), core);
        }

        /**
         * Creates a new virtual host mapping.
         * 
         * @param name Host name.
         * @param appBase The "webapp" directory.
         * @return Always non-null.
         */
        public Host createHost(String name, File appBase)
        {
            return new Host(invoke(embeddedCreateHost, name, appBase.getAbsolutePath()));
        }

        /**
         * Starts the container.
         */
        public void start()
        {
            invoke(embeddedStart);
        }

        /**
         * Stops the container.
         */
        public void stop()
        {
            invoke(embeddedStop);
        }

        /**
         * Adds a new {@link Engine}.
         * 
         * @param e must be non-null.
         */
        public void addEngine(Engine e)
        {
            invoke(embeddedAddEngine, e);
        }

        /**
         * Creates a new connector.
         * 
         * @param inetAddress non-null if you want to bind to specific interfaces
         * @param port TCP port number.
         * @param secure Not sure what this really is.
         * @return Always non-null.
         */
        public Connector createConnector(InetAddress inetAddress, int port, boolean secure)
        {
            return new Connector(invoke(embeddedCreateConnector, inetAddress, port, secure));
        }

        /**
         * Adds a connector.
         * 
         * @param connector must be non-null.
         */
        public void addConnector(Connector connector)
        {
            invoke(embeddedAddConnector, connector);
        }

        /**
         * Creates an web application for deployment.
         * 
         * @param path the context URL
         * @param docBase the exploded war file image.
         * @return Always non-null
         */
        public Context createContext(String path, String docBase)
        {
            return new Context(invoke(embeddedCreateContext, path,
                new File(docBase).getAbsolutePath()));
        }

        /**
         * Associates a realm to Tomcat.
         * 
         * @param realm realm object.
         */
        public void setRealm(MemoryRealm realm)
        {
            invoke(embeddedSetRealm, realm);
        }

        /**
         * Sets the Tomcat installation where catalina jars are loaded from.
         * 
         * @param dir the home directory.
         */
        public void setCatalinaBase(File dir)
        {
            invoke(embeddedSetCatalinaBase, dir.getAbsolutePath());
        }

        /**
         * Sets the directory where Tomcat stores data file for the current running instance.
         * 
         * @param dir the home directory.
         */
        public void setCatalinaHome(File dir)
        {
            invoke(embeddedSetCatalinaHome, dir.getAbsolutePath());
        }

        /**
         * Sets the HTTP port number.
         * 
         * @param port Port.
         */
        public void setPort(int port)
        {
            invoke(embeddedSetPort, port);
        }

        /**
         * Enable JNDI naming.
         */
        public void enableNaming()
        {
            invoke(embeddedEnableNaming);
        }

        /**
         * Gets the connector.
         * 
         * @return Connector.
         */
        public Connector getConnector()
        {
            return new Connector(invoke(embeddedGetConnector));
        }

        /**
         * Gets the engine.
         * 
         * @return Engine.
         */
        public Engine getEngine()
        {
            return new Engine(invoke(embeddedGetEngine), null);
        }

        /**
         * Gets the host.
         * 
         * @return Host.
         */
        public Host getHost()
        {
            return new Host(invoke(embeddedGetHost));
        }
    }

    /**
     * Copy of <code>org.apache.catalina.Connector</code>
     */
    public final class Connector extends Wrapper
    {
        /**
         * Wraps a connector object.
         * 
         * @param core object to be wrapped.
         */
        public Connector(Object core)
        {
            super(core);
        }

        /**
         * Stops the connector.
         */
        public void destroy()
        {
            invoke(connectorDestroy);
        }
    }

    /**
     * Copy of <code>org.apache.catalina.Engine</code>
     */
    public final class Engine extends Wrapper
    {
        /**
         * Wraps an engine object.
         * 
         * @param core Must be non-null.
         * @param service Tomcat service.
         */
        public Engine(Object core, Object service)
        {
            super(core);
            if (service != null)
            {
                invoke(engineSetService, service);
            }
        }

        /**
         * Assigns a name to engine.
         * 
         * @param name non-null
         */
        public void setName(String name)
        {
            invoke(engineSetName, name);
        }

        /**
         * Sets the directory that Tomcat will use as a workspace.
         * 
         * @param baseDir The directory name.
         */
        public void setBaseDir(String baseDir)
        {
            invoke(standardEngineSetBaseDir, baseDir);
        }

        /**
         * Adds a new {@link Host} to the engine.
         * 
         * @param host must be non-null.
         */
        public void addChild(Host host)
        {
            invoke(engineAddChild, host);
        }

        /**
         * Don't know what it really does.
         * 
         * @param name host name.
         */
        public void setDefaultHost(String name)
        {
            invoke(engineSetDefaultHost, name);
        }

        /**
         * Sets the {@link ClassLoader} that this engine will delegate to.
         * 
         * @param cl This needs to be set to {@link ClassLoader} that can see classes that implement
         * Tomcat, or else you'll get errors like "Servlet jsp is not available" (because the system
         * failed to load <code>JspServlet</code> class.)
         */
        public void setParentClassLoader(ClassLoader cl)
        {
            invoke(engineSetParentClassLoader, cl);
        }

        /**
         * Set the realm.
         * 
         * @param realm Realm to set.
         */
        public void setRealm(MemoryRealm realm)
        {
            invoke(engineSetRealm, realm);
        }
    }

    /**
     * MemoryRealm wrapper.
     */
    public final class MemoryRealm extends Wrapper
    {
        /**
         * Creates a new {@link MemoryRealm}.
         */
        public MemoryRealm()
        {
            super(newInstance(memoryRealmNew));
        }

        /**
         * Sets the file to load username/password.
         * 
         * @param path The user database file.
         */
        public void setPathname(File path)
        {
            invoke(memoryRealmSetPathname, path.toString());
        }
    }

    /**
     * Creates a new instance.
     * 
     * @param c Constructor to invoke.
     * @return The created object.
     */
    private Object newInstance(Constructor c)
    {
        // why do we set context class loader? see the comment inside the constructor
        // about commons logging.
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);

        try
        {
            return c.newInstance(new Object[0]);
        }
        catch (InstantiationException | InvocationTargetException | IllegalAccessException e)
        {
            throw new ContainerException("Tomcat reported an error: " + e.getMessage(), e);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }
}
