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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.jetty.internal.JettyUtils;

/**
 * A Jetty 7.x instance running embedded.
 */
public class Jetty7xEmbeddedLocalContainer extends Jetty6xEmbeddedLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jetty7x";

    /**
     * {@inheritDoc}
     * @see Jetty6xEmbeddedLocalContainer#Jetty6xEmbeddedLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Jetty7xEmbeddedLocalContainer(LocalConfiguration configuration)
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
     * If a default realm is available, set it on the given webapp.
     * 
     * @param webapp the webapp to set the realm on
     * @throws Exception on invokation error
     */
    @Override
    public void setDefaultRealm(Object webapp) throws Exception
    {
        if (this.defaultRealm != null)
        {
            Object securityHandler =
                webapp.getClass().getMethod("getSecurityHandler").invoke(webapp);

            Class userRealmClass = getClassLoader()
                .loadClass("org.eclipse.jetty.security.LoginService");
            securityHandler.getClass().getMethod("setLoginService", userRealmClass)
                .invoke(securityHandler, this.defaultRealm);

            Class authenticatorClass = getClassLoader()
                .loadClass("org.eclipse.jetty.security.Authenticator");
            Object basicAuthenticator = getClassLoader()
                .loadClass("org.eclipse.jetty.security.authentication.BasicAuthenticator")
                    .getDeclaredConstructor().newInstance();
            securityHandler.getClass().getMethod("setAuthenticator", authenticatorClass)
                    .invoke(securityHandler, basicAuthenticator);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configureJettyConnectors() throws ClassNotFoundException,
        InstantiationException, IllegalAccessException, InvocationTargetException,
        NoSuchMethodException
    {
        // Connector selectConnector = new SelectChannelConnector();
        // selectConnector.setPort(new
        // Integer(getConfiguration().getPropertyValue(ServletPropertySet.PORT)));
        Class selectConnectorClass =
            getClassLoader().loadClass("org.eclipse.jetty.server.nio.SelectChannelConnector");
        Object connector = selectConnectorClass.getDeclaredConstructor().newInstance();
        selectConnectorClass.getMethod("setPort", int.class).invoke(connector,
            Integer.parseInt(getConfiguration() .getPropertyValue(ServletPropertySet.PORT)));

        // server.addConnector(selectConnector);
        Class connectorClass = getClassLoader().loadClass("org.eclipse.jetty.server.Connector");
        Object connectorArray = Array.newInstance(connectorClass, 1);
        Array.set(connectorArray, 0, connector);
        getServer().getClass().getMethod("addConnector", connectorClass)
            .invoke(getServer(), connector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addJettyHandlers() throws ClassNotFoundException, InstantiationException,
        IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        // Set up the context handler structure
        // HandlerCollection handlers = new HandlerCollection();
        // ContextHandlerCollection contextHandlers = new ContextHandlerCollection();
        // handlers.setHandlers(new Handler[]{contextHandlers, new DefaultHandler(), new
        // RequestLogHandler()});
        // server.setHandler(handlers);
        handlerClass = getClassLoader().loadClass("org.eclipse.jetty.server.Handler");
        handlers =
            getClassLoader().loadClass("org.eclipse.jetty.server.handler.HandlerCollection")
                .getDeclaredConstructor().newInstance();
        contextHandlers =
            getClassLoader().loadClass(
                "org.eclipse.jetty.server.handler.ContextHandlerCollection")
                    .getDeclaredConstructor().newInstance();
        Object defaultHandler =
            getClassLoader().loadClass("org.eclipse.jetty.server.handler.DefaultHandler")
                .getDeclaredConstructor().newInstance();
        Object handlerArray = Array.newInstance(handlerClass, 2);
        Array.set(handlerArray, 0, contextHandlers);
        Array.set(handlerArray, 1, defaultHandler);
        handlers.getClass().getMethod("setHandlers", handlerArray.getClass())
            .invoke(handlers, handlerArray);
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
    public Object createHandler(Deployable deployable) throws Exception
    {
        Object handler =
            getClassLoader().loadClass("org.eclipse.jetty.webapp.WebAppContext")
                .getDeclaredConstructor().newInstance();

        handler.getClass().getMethod("setContextPath", String.class)
            .invoke(handler, "/" + ((WAR) deployable).getContext());
        handler.getClass().getMethod("setWar", String.class).invoke(handler, deployable.getFile());
        handler.getClass().getMethod("setDefaultsDescriptor", String.class).invoke(handler,
            getFileHandler().append(getConfiguration().getHome(), "etc/webdefault.xml"));
        handler.getClass().getMethod("setExtraClasspath", String.class)
            .invoke(handler, JettyUtils.getExtraClasspath((WAR) deployable, false));

        setDefaultRealm(handler);

        return handler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createHandler(String contextPath, String war) throws Exception
    {
        Object handler =
            getClassLoader().loadClass("org.eclipse.jetty.webapp.WebAppContext")
                .getDeclaredConstructor().newInstance();
        handler.getClass().getMethod("setContextPath", String.class).invoke(handler, contextPath);
        handler.getClass().getMethod("setWar", String.class).invoke(handler, war);

        setDefaultRealm(handler);

        return handler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setSecurityRealm() throws Exception
    {
        if (getConfiguration().getUsers() != null && !getConfiguration().getUsers().isEmpty())
        {
            Class realmClass =
                getClassLoader().loadClass("org.eclipse.jetty.security.HashLoginService");

            Class credentialClass;
            try
            {
                // Name until Jetty 7.5 (inclusive) and Jetty 8.0
                credentialClass = getClassLoader()
                    .loadClass("org.eclipse.jetty.http.security.Credential");
            }
            catch (ClassNotFoundException e)
            {
                // Name after Jetty 7.6 and Jetty 8.1
                credentialClass = getClassLoader()
                    .loadClass("org.eclipse.jetty.util.security.Credential");
            }
            try
            {
                this.defaultRealm =
                    realmClass.getConstructor(String.class).newInstance(
                        getConfiguration().getPropertyValue(JettyPropertySet.REALM_NAME));
                Method putUser = realmClass.getMethod(
                    "putUser", java.lang.String.class, credentialClass, java.lang.String[].class);
                for (User user : getConfiguration().getUsers())
                {
                    String userName = user.getName();
                    Object credential = credentialClass.getMethod("getCredential", String.class)
                        .invoke(credentialClass, user.getPassword());
                    String[] roles = user.getRoles().toArray(new String[user.getRoles().size()]);

                    putUser.invoke(this.defaultRealm, userName, credential, roles);
                }
            }
            catch (NoSuchMethodException e)
            {
                // Method putUser was removed with Jetty 9.4
                String etcDir = getFileHandler().append(getConfiguration().getHome(), "etc");
                JettyUtils.createRealmFile(
                    getConfiguration().getUsers(), etcDir, getFileHandler());
                this.defaultRealm =
                    realmClass.getConstructor(String.class, String.class).newInstance(
                        getConfiguration().getPropertyValue(JettyPropertySet.REALM_NAME),
                            getFileHandler().append(etcDir, "cargo-realm.properties"));
            }

            Object userRealmsArray =
                Array.newInstance(getClassLoader().loadClass(
                    "org.eclipse.jetty.security.LoginService"), 1);
            Array.set(userRealmsArray, 0, this.defaultRealm);
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
            try
            {
                this.server = getClassLoader().loadClass("org.eclipse.jetty.server.Server")
                    .getDeclaredConstructor().newInstance();
            }
            catch (Exception e)
            {
                throw new ContainerException("Failed to create Jetty Server instance", e);
            }

            this.server.getClass().getMethod("setStopAtShutdown", boolean.class)
                .invoke(this.server, Boolean.TRUE);
        }
    }
}
