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
 * 
 * @version $Id$
 */
public class Jetty7xEmbeddedLocalContainer extends Jetty6xEmbeddedLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jetty7x";

    /**
     * {@inheritDoc}
     * 
     * @see Jetty6xEmbeddedLocalContainer#Jetty6xEmbeddedLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Jetty7xEmbeddedLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.Container#getId()
     */
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
    public void setDefaultRealm(Object webapp) throws Exception
    {
        // Class userRealmClass = getClassLoader()
        // .loadClass("org.eclipse.jetty.security.UserRealm");
        Class userRealmClass = getClassLoader()
            .loadClass("org.eclipse.jetty.security.LoginService");

        if (this.defaultRealm != null)
        {
            Object securityHandler =
                webapp.getClass().getMethod("getSecurityHandler", new Class[] {}).invoke(webapp,
                    new Object[] {});
            securityHandler.getClass().getMethod("setLoginService", new Class[] {userRealmClass})
                .invoke(securityHandler, new Object[] {this.defaultRealm});
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
        Object connector = selectConnectorClass.newInstance();
        selectConnectorClass.getMethod("setPort", new Class[] {int.class}).invoke(
            connector,
            new Object[] {new Integer(getConfiguration()
                .getPropertyValue(ServletPropertySet.PORT))});

        // server.addConnector(selectConnector);
        Class connectorClass = getClassLoader().loadClass("org.eclipse.jetty.server.Connector");
        Object connectorArray = Array.newInstance(connectorClass, 1);
        Array.set(connectorArray, 0, connector);
        getServer().getClass().getMethod("addConnector", new Class[] {connectorClass})
            .invoke(getServer(), new Object[] {connector});
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
                .newInstance();
        contextHandlers =
            getClassLoader().loadClass(
                "org.eclipse.jetty.server.handler.ContextHandlerCollection").newInstance();
        Object defaultHandler =
            getClassLoader().loadClass("org.eclipse.jetty.server.handler.DefaultHandler")
                .newInstance();
        Object handlerArray = Array.newInstance(handlerClass, 2);
        Array.set(handlerArray, 0, contextHandlers);
        Array.set(handlerArray, 1, defaultHandler);
        handlers.getClass().getMethod("setHandlers", new Class[] {handlerArray.getClass()})
            .invoke(handlers, new Object[] {handlerArray});
        getServer().getClass().getMethod("setHandler", new Class[] {handlerClass})
            .invoke(getServer(), new Object[] {handlers});

        // Method to add a webappcontext to jetty
        addHandlerMethod =
            contextHandlers.getClass().getMethod("addHandler", new Class[] {handlerClass});

        // Method to remove a webappcontext from jetty
        removeHandlerMethod =
            contextHandlers.getClass().getMethod("removeHandler", new Class[] {handlerClass});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createHandler(Deployable deployable) throws Exception
    {
        Object handler =
            getClassLoader().loadClass("org.eclipse.jetty.webapp.WebAppContext").newInstance();

        handler.getClass().getMethod("setContextPath", new Class[] {String.class}).invoke(
            handler, new Object[] {"/" + ((WAR) deployable).getContext()});
        handler.getClass().getMethod("setWar", new Class[] {String.class}).invoke(handler,
            new Object[] {deployable.getFile()});
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
            getClassLoader().loadClass("org.eclipse.jetty.webapp.WebAppContext").newInstance();
        handler.getClass().getMethod("setContextPath", new Class[] {String.class}).invoke(
            handler, new Object[] {contextPath});
        handler.getClass().getMethod("setWar", new Class[] {String.class}).invoke(handler,
            new Object[] {war});

        setDefaultRealm(handler);

        return handler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setSecurityRealm() throws Exception
    {
        if (getConfiguration().getPropertyValue(ServletPropertySet.USERS) != null)
        {
            Class realmClass =
                // see: http://wiki.eclipse.org/Jetty/Starting/Porting_to_Jetty_7
                getClassLoader().loadClass("org.eclipse.jetty.security.HashLoginService");
            this.defaultRealm =
                realmClass.getConstructor(new Class[] {String.class}).newInstance(
                    new Object[] {"Cargo Test Realm"});

            for (User user : User.parseUsers(getConfiguration().getPropertyValue(
                ServletPropertySet.USERS)))
            {
                String userName = user.getName();
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
                Object credential = credentialClass.getMethod("getCredential", String.class)
                    .invoke(credentialClass, user.getPassword());
                String[] roles = user.getRoles().toArray(new String[user.getRoles().size()]);

                Method putUser =
                    this.defaultRealm.getClass().getMethod("putUser",
                        new Class[] {java.lang.String.class,
                            credentialClass, java.lang.String[].class});
                putUser.invoke(this.defaultRealm,
                    new Object[] {userName, credential, roles});
            }

            Object userRealmsArray =
                Array.newInstance(getClassLoader().loadClass(
                    "org.eclipse.jetty.security.LoginService"), 1);
            Array.set(userRealmsArray, 0, this.defaultRealm);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.jetty.internal.AbstractJettyEmbeddedLocalContainer#createServerObject()
     */
    @Override
    protected synchronized void createServerObject() throws Exception
    {
        if (this.server == null)
        {
            try
            {
                this.server = getClassLoader().loadClass("org.eclipse.jetty.server.Server")
                    .newInstance();
            }
            catch (Exception e)
            {
                throw new ContainerException("Failed to create Jetty Server instance", e);
            }

            this.server.getClass().getMethod("setStopAtShutdown", new Class[] {boolean.class})
                .invoke(this.server, new Object[] {Boolean.TRUE});
        }
    }
}
