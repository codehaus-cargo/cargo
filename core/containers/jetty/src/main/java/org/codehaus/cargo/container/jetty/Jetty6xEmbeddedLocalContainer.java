/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.jetty.internal.AbstractJettyEmbeddedLocalContainer;
import org.codehaus.cargo.container.jetty.internal.JettyExecutorThread;
import org.codehaus.cargo.container.jetty.internal.JettyUtils;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.User;

/**
 * A Jetty 6.x instance running embedded.
 */
public class Jetty6xEmbeddedLocalContainer extends AbstractJettyEmbeddedLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jetty6x";

    /**
     * A default security realm. If ServletPropertySet.USERS has been specified, then we create a
     * default realm containing those users and then force that realm to be associated with every
     * webapp (see TODO comment on setSecurityRealm())
     */
    protected Object defaultRealm;

    /**
     * The ContextHandlerCollection into which deployed webapps are added.
     */
    protected Object contextHandlers;

    /**
     * The org.mortbay.jetty.handler.Handler class.
     */
    protected Class handlerClass;

    /**
     * The org.mortbay.jetty.handler.HandlerCollection instance.
     */
    protected Object handlers;

    /**
     * The method to call to add a handler for a webapp.
     */
    protected Method addHandlerMethod;

    /**
     * The method to call to undeploy a handler for a webapp.
     */
    protected Method removeHandlerMethod;

    /**
     * {@inheritDoc}
     * @see AbstractJettyEmbeddedLocalContainer#AbstractJettyEmbeddedLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Jetty6xEmbeddedLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
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
    public String getName()
    {
        return "Jetty " + getVersion();
    }

    /**
     * If a default realm is available, set it on the given webapp.
     * 
     * @param webapp the webapp to set the realm on
     * @throws Exception on invokation error
     */
    public void setDefaultRealm(Object webapp) throws Exception
    {
        Class userRealmClass = getClassLoader().loadClass("org.mortbay.jetty.security.UserRealm");

        if (this.defaultRealm != null)
        {
            Object securityHandler =
                webapp.getClass().getMethod("getSecurityHandler").invoke(webapp);
            securityHandler.getClass().getMethod("setUserRealm", userRealmClass)
                .invoke(securityHandler, this.defaultRealm);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStart() throws Exception
    {
        createServerObject();
        configureJettyConnectors();
        setSecurityRealm();
        addJettyHandlers();
        addDeployables();
        startJetty();
    }

    /**
     * @return The Jetty Webapp context class name.
     */
    protected String getWebappContextClassname()
    {
        return "org.mortbay.jetty.webapp.WebAppContext";
    }

    /**
     * Configure Jetty connectors.
     * 
     * @throws ClassNotFoundException thrown if the connectors could not be configured
     * @throws InstantiationException thrown if the connectors could not be configured
     * @throws IllegalAccessException thrown if the connectors could not be configured
     * @throws InvocationTargetException thrown if the connectors could not be configured
     * @throws NoSuchMethodException thrown if the connectors could not be configured
     */
    protected void configureJettyConnectors() throws ClassNotFoundException,
        InstantiationException, IllegalAccessException, InvocationTargetException,
        NoSuchMethodException
    {
        // Connector selectConnector = new SelectChannelConnector();
        // selectConnector.setPort(
        //     new Integer(getConfiguration().getPropertyValue(ServletPropertySet.PORT)));
        Class selectConnectorClass =
            getClassLoader().loadClass("org.mortbay.jetty.nio.SelectChannelConnector");
        Object connector = selectConnectorClass.getDeclaredConstructor().newInstance();
        selectConnectorClass.getMethod("setPort", int.class).invoke(connector,
            Integer.parseInt(getConfiguration().getPropertyValue(ServletPropertySet.PORT)));

        // server.addConnector(selectConnector);
        Class connectorClass = getClassLoader().loadClass("org.mortbay.jetty.Connector");
        Object connectorArray =
            Array.newInstance(connectorClass, 1);
        Array.set(connectorArray, 0, connector);
        getServer().getClass().getMethod("addConnector", connectorClass).invoke(
            getServer(), connector);
    }

    /**
     * Add Jetty handlers.
     * 
     * @throws ClassNotFoundException thrown if the handlers could not be added
     * @throws InstantiationException thrown if the handlers could not be added
     * @throws IllegalAccessException thrown if the handlers could not be added
     * @throws InvocationTargetException thrown if the handlers could not be added
     * @throws NoSuchMethodException thrown if the handlers could not be added
     */
    protected void addJettyHandlers() throws ClassNotFoundException, InstantiationException,
        IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        // Set up the context handler structure
        // HandlerCollection handlers = new HandlerCollection();
        // ContextHandlerCollection contextHandlers = new ContextHandlerCollection();
        // handlers.setHandlers(
        //     new Handler[]{contextHandlers, new DefaultHandler()});
        // server.setHandler(handlers);
        handlerClass = getClassLoader().loadClass("org.mortbay.jetty.Handler");
        handlers =
            getClassLoader().loadClass("org.mortbay.jetty.handler.HandlerCollection")
                .getDeclaredConstructor().newInstance();
        contextHandlers =
            getClassLoader().loadClass("org.mortbay.jetty.handler.ContextHandlerCollection")
                .getDeclaredConstructor().newInstance();
        Object defaultHandler =
            getClassLoader().loadClass("org.mortbay.jetty.handler.DefaultHandler")
                .getDeclaredConstructor().newInstance();
        Object handlerArray = Array.newInstance(handlerClass, 2);
        Array.set(handlerArray, 0, contextHandlers);
        Array.set(handlerArray, 1, defaultHandler);
        handlers.getClass().getMethod("setHandlers", handlerArray.getClass())
            .invoke(handlers, handlerArray);
        getServer().getClass().getMethod("setHandler", handlerClass).invoke(getServer(), handlers);

        // Method to add a webappcontext to jetty
        addHandlerMethod =
            contextHandlers.getClass().getMethod("addHandler", handlerClass);

        // Method to remove a webappcontext from jetty
        removeHandlerMethod =
            contextHandlers.getClass().getMethod("removeHandler", handlerClass);
    }

    /**
     * Add the cargo deployables and the Cargo Ping Check.
     * 
     * @throws Exception thrown if the deployables could not be added
     */
    protected void addDeployables() throws Exception
    {
        // Deploy statically deployed WARs
        for (Deployable deployable : getConfiguration().getDeployables())
        {
            // Only deploy WARs (packed or unpacked).
            if (deployable.getType() == DeployableType.WAR)
            {
                addHandler(createHandler(deployable));
            }
            else
            {
                throw new ContainerException("Only WAR archives are supported for deployment in "
                    + "Jetty. Got [" + deployable.getFile() + "]");
            }
        }

        // Deploy CPC. Note: The Jetty Server class offers a isStarted()
        // method but there is no isStopped() so until we find a better
        // way, we need a CPC.

        addHandler(createHandler("/cargocpc", new File(getConfiguration().getHome(),
            "cargocpc.war").getPath()));
    }

    /**
     * Create a WebAppContext for the Deployable. NB also force the defaultRealm to be set on it if
     * one is present.
     * 
     * @param deployable the cargo webapp to deploy
     * @return a jetty webapp
     * @throws Exception on invokation exception
     */
    public Object createHandler(Deployable deployable) throws Exception
    {
        Object handler =
            getClassLoader().loadClass(getWebappContextClassname())
                .getDeclaredConstructor().newInstance();

        handler.getClass().getMethod("setContextPath", String.class)
            .invoke(handler, "/" + ((WAR) deployable).getContext());
        handler.getClass().getMethod("setWar", String.class).invoke(handler, deployable.getFile());
        handler.getClass().getMethod("setDefaultsDescriptor", String.class).invoke(handler,
            getFileHandler().append(getConfiguration().getHome(), "etc/webdefault.xml"));
        String extraClasspath = JettyUtils.getExtraClasspath((WAR) deployable, false);
        if (extraClasspath != null)
        {
            handler.getClass().getMethod("setExtraClasspath", String.class)
                .invoke(handler, extraClasspath);
        }

        setDefaultRealm(handler);

        return handler;
    }

    /**
     * Create a WebAppContext for the webapp given as a string. NB Also force the defaultRealm to be
     * set if one is present.
     * 
     * @param contextPath the context path for the webapp
     * @param war the webapp
     * @return a jetty webapp
     * @throws Exception on invokation exception
     */
    public Object createHandler(String contextPath, String war) throws Exception
    {
        Object handler =
            getClassLoader().loadClass(getWebappContextClassname())
                .getDeclaredConstructor().newInstance();
        handler.getClass().getMethod("setContextPath", String.class).invoke(handler, contextPath);
        handler.getClass().getMethod("setWar", String.class).invoke(handler, war);

        setDefaultRealm(handler);

        return handler;
    }

    /**
     * Deploy the handler representing the webapp to jetty. If jetty is already started, then start
     * the handler.
     * 
     * @param handler the handler representing the webapp
     * @throws Exception on invocation exception
     */
    public void addHandler(Object handler) throws Exception
    {
        if (addHandlerMethod == null)
        {
            throw new ContainerException("No Jetty instance to deploy to");
        }
        addHandlerMethod.invoke(contextHandlers, handler);
        Method m = getServer().getClass().getMethod("isStarted");
        if ((Boolean) m.invoke(getServer()))
        {
            handlerClass.getMethod("start").invoke(handler);
        }
    }

    /**
     * Undeploy the handler representing the webapp.
     * 
     * @param handler the handler representing the webapp
     * @throws Exception on invocation exception
     */
    public void removeHandler(Object handler) throws Exception
    {
        if (handler == null)
        {
            return;
        }

        if (removeHandlerMethod == null)
        {
            throw new ContainerException("No Jetty instance to deploy to");
        }
        removeHandlerMethod.invoke(contextHandlers, handler);
    }

    /**
     * Defines a security realm and adds defined users to it. If a user has specified the standard
     * ServletPropertySet.USERS property, then we try and turn these into an in-memory default
     * realm, and then set that realm on all of the webapps. TODO: this is not ideal. We need a way
     * to specify N named realms to the server so that individual webapps can find their appropriate
     * realms by name.
     * 
     * @throws Exception in case of error
     */
    protected void setSecurityRealm() throws Exception
    {
        if (!getConfiguration().getUsers().isEmpty())
        {
            Class realmClass =
                getClassLoader().loadClass("org.mortbay.jetty.security.HashUserRealm");
            this.defaultRealm =
                realmClass.getConstructor(String.class).newInstance(
                    getConfiguration().getPropertyValue(JettyPropertySet.REALM_NAME));

            for (User user : getConfiguration().getUsers())
            {
                this.defaultRealm.getClass().getMethod("put", Object.class, Object.class)
                    .invoke(this.defaultRealm, user.getName(), user.getPassword());

                for (String role : user.getRoles())
                {
                    this.defaultRealm.getClass()
                        .getMethod("addUserToRole", String.class, String.class)
                            .invoke(this.defaultRealm, user.getName(), role);
                }
            }

            Object userRealmsArray =
                Array.newInstance(getClassLoader().loadClass(
                    "org.mortbay.jetty.security.UserRealm"), 1);
            Array.set(userRealmsArray, 0, this.defaultRealm);

            // Add newly created realm to server
            getServer().getClass().getMethod("setUserRealms", userRealmsArray.getClass())
                .invoke(getServer(), userRealmsArray);

            Object[] realmlist =
                (Object[]) getServer().getClass().getMethod("getUserRealms").invoke(getServer());
            getLogger().info(
                "Added " + (realmlist == null ? "0" : String.valueOf(realmlist.length))
                    + " realms ", getClass().getName());
        }
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

            this.server.getClass().getMethod("setStopAtShutdown", boolean.class)
                .invoke(this.server, Boolean.TRUE);
        }
    }

    /**
     * Starts the Jetty server.
     */
    protected void startJetty()
    {
        JettyExecutorThread jettyRunner = new JettyExecutorThread(getServer(), true);
        jettyRunner.setLogger(getLogger());
        jettyRunner.start();
    }
}
