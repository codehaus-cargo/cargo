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
import java.util.List;
import java.util.ArrayList;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
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
