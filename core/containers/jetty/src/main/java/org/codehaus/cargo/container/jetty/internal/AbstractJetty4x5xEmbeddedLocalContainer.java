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
package org.codehaus.cargo.container.jetty.internal;

import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.ContainerException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.io.File;

/**
 * Common code for all Jetty 4.x and 5.x embedded container implementations.
 *
 * @version $Id$
 */
public abstract class AbstractJetty4x5xEmbeddedLocalContainer
    extends AbstractJettyEmbeddedLocalContainer
{
    /**
     * {@inheritDoc}
     * @see AbstractJettyEmbeddedLocalContainer#AbstractEmbeddedLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public AbstractJetty4x5xEmbeddedLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see AbstractJetty4x5xEmbeddedLocalContainer#doStart()
     * @todo Extract code for standalone configuration and put it in
     * {@link AbstractJettyStandaloneLocalConfiguration}
     */
    @Override
    protected void doStart() throws Exception
    {
        createServerObject();

        // Configure a listener
        Class listenerClass = getClassLoader().loadClass("org.mortbay.http.SocketListener");
        Object listener = listenerClass.newInstance();

        listenerClass.getMethod("setPort", new Class[] {int.class}).invoke(listener,
            new Object[] {new Integer(getConfiguration().getPropertyValue(
                ServletPropertySet.PORT))});

        getServer().getClass().getMethod("addListener",
            new Class[] {getClassLoader().loadClass("org.mortbay.http.HttpListener")})
            .invoke(getServer(), new Object[] {listener});

        // Set up security realm
        setSecurityRealm();

        // Deploy WAR deployables
        Iterator it = getConfiguration().getDeployables().iterator();
        while (it.hasNext())
        {
            Deployable deployable = (Deployable) it.next();

            // Only deploy WARs.
            if (deployable.getType() == DeployableType.WAR)
            {
                Object webapp = getServer().getClass().getMethod("addWebApplication",
                    new Class[] {String.class, String.class}).invoke(getServer(),
                        new Object[] {"/" + ((WAR) deployable).getContext(), deployable.getFile()});
                performExtraSetupOnDeployable(webapp);
            }
            else
            {
                throw new ContainerException("Only WAR archives are supported for deployment in "
                    + "Jetty. Got [" + deployable.getFile() + "]");
            }
        }

        // Deploy CPC. Note: The Jetty Server class offers a isStarted() method but there is no
        // isStopped() so until we find a better way, we need a CPC.
        getServer().getClass().getMethod("addWebApplication",
            new Class[] {String.class, String.class}).invoke(getServer(),
                new Object[] {"/cargocpc", new File(getConfiguration().getHome(),
                    "cargocpc.war").getPath()});

        JettyExecutorThread jettyRunner = new JettyExecutorThread(getServer(), true);
        jettyRunner.setLogger(getLogger());
        jettyRunner.start();
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractLocalContainer#waitForCompletion(boolean)
     */
    @Override
    protected void waitForCompletion(boolean waitForStarting) throws InterruptedException
    {
        if (waitForStarting)
        {
            long timeout = System.currentTimeMillis() + this.getTimeout();
            while (System.currentTimeMillis() < timeout)
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    throw new IllegalStateException("Thread.sleep failed");
                }

                Method isStarted;
                try
                {
                    isStarted = getServer().getClass().getMethod("isStarted", null);
                }
                catch (NoSuchMethodException e)
                {
                    throw new ContainerException("Cannot find method isStarted", e);
                }
                Boolean started;
                try
                {
                    started = (Boolean) isStarted.invoke(getServer(), null);
                }
                catch (IllegalAccessException e)
                {
                    throw new ContainerException("Cannot execute method isStarted", e);
                }
                catch (InvocationTargetException e)
                {
                    throw new ContainerException("Cannot execute method isStarted", e);
                }
                if (started)
                {
                    return;
                }
            }

            throw new ContainerException("Server did not start after "
                    + Long.toString(this.getTimeout()) + " milliseconds");
        }
        else
        {
            super.waitForCompletion(waitForStarting);
        }
    }

    /**
     * Allow extending classes to perform extra setup on the web application object.
     *
     * @param webapp the Jetty web application object representing the WAR that has been added to
     *        be deployed
     * @throws Exception in case of error
     */
    protected abstract void performExtraSetupOnDeployable(Object webapp) throws Exception;

    /**
     * Defines a security realm and adds defined users to it. If a user has specified the standard
     * ServletPropertySet.USERS property, then we try and turn these into an in-memory default
     * realm, and then set that realm on all of the webapps.
     *
     * TODO: this is not ideal. We need a way to specify N named realms to the server so that
     * individual webapps can find their appropriate realms by name.
     *
     * @throws Exception in case of error
     */
    protected void setSecurityRealm() throws Exception
    {
        if (getConfiguration().getPropertyValue(ServletPropertySet.USERS) != null)
        {
            Class realmClass = getClassLoader().loadClass("org.mortbay.http.HashUserRealm");
            Object defaultRealm = realmClass.getConstructor(
                new Class[] {String.class}).newInstance(new Object[] {"Cargo Test Realm"});

            Iterator users = User.parseUsers(
                getConfiguration().getPropertyValue(ServletPropertySet.USERS)).iterator();
            while (users.hasNext())
            {
                User user = (User) users.next();

                defaultRealm.getClass().getMethod("put",
                    new Class[] {Object.class, Object.class}).invoke(defaultRealm,
                        new Object[] {user.getName(), user.getPassword()});

                Iterator roles = user.getRoles().iterator();
                while (roles.hasNext())
                {
                    String role = (String) roles.next();

                    defaultRealm.getClass().getMethod("addUserToRole",
                        new Class[] {String.class, String.class}).invoke(
                            defaultRealm, new Object[] {user.getName(), role});
                }
            }

            // Add newly created realm to server
            getServer().getClass().getMethod("addRealm",
                new Class[] {getClassLoader().loadClass("org.mortbay.http.UserRealm")})
                    .invoke(getServer(), new Object[] {defaultRealm});
        }
    }
}
