/*
 * ========================================================================
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
package org.codehaus.cargo.container.jetty;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.util.CargoException;

/**
 * A Jetty 12.x instance running embedded.
 */
public class Jetty12xEmbeddedLocalContainer extends Jetty11xEmbeddedLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jetty12x";

    /**
     * {@inheritDoc}
     * @see Jetty11xEmbeddedLocalContainer#Jetty11xEmbeddedLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Jetty12xEmbeddedLocalContainer(LocalConfiguration configuration)
    {
        // Ensure that the configuration provided is Jetty 12.x
        super((Jetty12xEmbeddedStandaloneLocalConfiguration) configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createHandler(Deployable deployable) throws Exception
    {
        return nestHandler(super.createHandler(deployable));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createHandler(String contextPath, String war) throws Exception
    {
        return nestHandler(super.createHandler(contextPath, war));
    }

    /**
     * Nest handler, necessary for supporting EE8 and EE9.
     * @param handler Handler created for Web context.
     * @return <code>handler</code> itself on EE10, nested version on other EE versions.
     * @throws Exception If anything goes wrong.
     */
    private Object nestHandler(Object handler) throws Exception
    {
        String eeVersion = getEeVersion();
        if (!"ee10".equals(eeVersion))
        {
            Class handlerClass =
                getClassLoader().loadClass(
                    "org.eclipse.jetty." + eeVersion + ".nested.Handler");
            Class nestedHandlerClass =
                getClassLoader().loadClass(
                    "org.eclipse.jetty." + eeVersion + ".nested.ContextHandler");
            Object nestedHandler =
                nestedHandlerClass.getDeclaredConstructor().newInstance();
            nestedHandlerClass.getMethod("setHandler", handlerClass).invoke(
                nestedHandler, handler);
            return nestedHandlerClass.getMethod("getCoreContextHandler").invoke(nestedHandler);
        }
        else
        {
            return handler;
        }
    }

    /**
     * @return EE version from the configuration.
     */
    private String getEeVersion()
    {
        return getConfiguration().getPropertyValue(JettyPropertySet.DEPLOYER_EE_VERSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized void createServerObject() throws Exception
    {
        if (this.server == null)
        {
            super.createServerObject();

            Class webAppClassLoadingClass;
            try
            {
                webAppClassLoadingClass =
                    getClassLoader().loadClass("org.eclipse.jetty.ee.WebAppClassLoading");
            }
            catch (ClassNotFoundException e)
            {
                webAppClassLoadingClass =
                    getClassLoader().loadClass("org.eclipse.jetty.ee.webapp.WebAppClassLoading");
            }

            if (webAppClassLoadingClass.getPackage().getImplementationVersion() != null
                && webAppClassLoadingClass.getPackage().getImplementationVersion().startsWith(
                    "12."))
            {
                // Override of the Jetty 12.x server classes list, to work around the nasty
                // java.lang.ClassNotFoundException:
                // org.eclipse.jetty.[ee8|ee9|ee10|ee11].servlet.listener.IntrospectorCleaner.
                Object defaultHiddenClasses =
                    webAppClassLoadingClass.getDeclaredField("DEFAULT_HIDDEN_CLASSES").get(null);
                Method remove = defaultHiddenClasses.getClass().getMethod("remove", Object.class);
                remove.invoke(defaultHiddenClasses, "org.eclipse.jetty.");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addJettyHandlers() throws ClassNotFoundException, InstantiationException,
        IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        // Set up the context handler structure
        // ContextHandlerCollection contextHandlers =
        //     new ContextHandlerCollection(true, new ContextHandler[0]);
        // Handler handlers = new Handler.Sequence(contextHandlers, new DefaultHandler());
        // server.setHandler(handlers);
        handlerClass = getClassLoader().loadClass("org.eclipse.jetty.server.Handler");
        Constructor[] contextHandlerConstructors =
            getClassLoader().loadClass(
                "org.eclipse.jetty.server.handler.ContextHandlerCollection")
                    .getDeclaredConstructors();
        for (Constructor contextHandlerConstructor : contextHandlerConstructors)
        {
            if (contextHandlerConstructor.getParameterTypes()[0].equals(Boolean.TYPE))
            {
                contextHandlers =
                    contextHandlerConstructor.newInstance(true,
                        Array.newInstance(getClassLoader().loadClass(
                            "org.eclipse.jetty.server.handler.ContextHandler"), 0));
                break;
            }
        }
        if (contextHandlers == null)
        {
            throw new CargoException("No valid ContextHandlerCollection constructor found");
        }

        Object defaultHandler =
            getClassLoader().loadClass("org.eclipse.jetty.server.handler.DefaultHandler")
                .getDeclaredConstructor().newInstance();
        List<Object> handlerList = new ArrayList<Object>(2);
        handlerList.add(contextHandlers);
        handlerList.add(defaultHandler);
        handlers =
            getClassLoader().loadClass("org.eclipse.jetty.server.Handler$Sequence")
                .getDeclaredConstructor(List.class).newInstance(handlerList);
        getServer().getClass().getMethod("setHandler", handlerClass)
            .invoke(getServer(), handlers);

        // Method to add a webappcontext to jetty
        addHandlerMethod =
            contextHandlers.getClass().getMethod("addHandler", handlerClass);

        // Method to remove a webappcontext from jetty
        removeHandlerMethod =
            contextHandlers.getClass().getMethod("removeHandler", handlerClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addAnnotationConfiguration() throws IllegalAccessException,
        InvocationTargetException, NoSuchMethodException
    {
        setAttributeMethod().invoke(
            getServer(),
            "org.eclipse.jetty." + getEeVersion() + ".webapp.configuration",
                new String[] {
                    "org.eclipse.jetty." + getEeVersion() + ".webapp.WebInfConfiguration",
                    "org.eclipse.jetty." + getEeVersion() + ".webapp.WebXmlConfiguration",
                    "org.eclipse.jetty." + getEeVersion() + ".webapp.MetaInfConfiguration",
                    "org.eclipse.jetty." + getEeVersion() + ".webapp.FragmentConfiguration",
                    "org.eclipse.jetty." + getEeVersion() + ".annotations.AnnotationConfiguration",
                    "org.eclipse.jetty." + getEeVersion() + ".webapp.JettyWebXmlConfiguration"});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getWebappContextClassname()
    {
        return "org.eclipse.jetty." + getEeVersion() + ".webapp.WebAppContext";
    }
}
