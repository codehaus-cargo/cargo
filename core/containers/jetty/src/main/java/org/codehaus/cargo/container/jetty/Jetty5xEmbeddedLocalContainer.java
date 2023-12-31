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
package org.codehaus.cargo.container.jetty;

import java.io.File;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.jetty.internal.AbstractJettyEmbeddedLocalContainer;
import org.codehaus.cargo.container.jetty.internal.JettyExecutorThread;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.User;

/**
 * A Jetty 5.x instance running embedded.
 */
public class Jetty5xEmbeddedLocalContainer extends AbstractJettyEmbeddedLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jetty5x";

    /**
     * A default security realm. If ServletPropertySet.USERS has been specified, then we create a
     * default realm containing those users and then force that realm to be associated with every
     * webapp (see TODO comment on setSecurityRealm()).
     */
    private Object defaultRealm;

    /**
     * {@inheritDoc}
     * @see AbstractJettyEmbeddedLocalContainer#AbstractJettyEmbeddedLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Jetty5xEmbeddedLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see AbstractJettyEmbeddedLocalContainer#doStart()
     */
    @Override
    protected void doStart() throws Exception
    {
        createServerObject();

        // Configure a listener
        Class listenerClass = getClassLoader().loadClass("org.mortbay.http.SocketListener");
        Object listener = listenerClass.getDeclaredConstructor().newInstance();

        listenerClass.getMethod("setPort", int.class).invoke(listener,
            Integer.parseInt(getConfiguration().getPropertyValue(ServletPropertySet.PORT)));

        getServer().getClass().getMethod("addListener",
            getClassLoader().loadClass("org.mortbay.http.HttpListener"))
                .invoke(getServer(), listener);

        // Set up security realm
        setSecurityRealm();

        String webdefault =
            new File(getConfiguration().getHome(), "etc/webdefault.xml").toURI().toString();

        // Deploy WAR deployables
        for (Deployable deployable : getConfiguration().getDeployables())
        {
            // Only deploy WARs.
            if (deployable.getType() == DeployableType.WAR)
            {
                Object webapp = getServer().getClass().getMethod("addWebApplication",
                    String.class, String.class).invoke(getServer(),
                        "/" + ((WAR) deployable).getContext(), deployable.getFile());
                webapp.getClass().getMethod("setDefaultsDescriptor", String.class)
                    .invoke(webapp, webdefault);
                setDefaultRealm(webapp);
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
            String.class, String.class).invoke(getServer(),
                "/cargocpc", new File(getConfiguration().getHome(), "cargocpc.war").getPath());

        JettyExecutorThread jettyRunner = new JettyExecutorThread(getServer(), true);
        jettyRunner.setLogger(getLogger());
        jettyRunner.start();
    }

    /**
     * Defines a security realm and adds defined users to it. If a user has specified the standard
     * ServletPropertySet.USERS property, then we try and turn these into an in-memory default
     * realm, and then set that realm on all of the webapps.<br>
     * <br>
     * TODO: this is not ideal. We need a way to specify N named realms to the server so that
     * individual webapps can find their appropriate realms by name.
     * 
     * @throws Exception in case of error
     */
    protected void setSecurityRealm() throws Exception
    {
        if (!getConfiguration().getUsers().isEmpty())
        {
            Class realmClass = getClassLoader().loadClass("org.mortbay.http.HashUserRealm");
            Object defaultRealm = realmClass.getConstructor(String.class).newInstance(
                    getConfiguration().getPropertyValue(JettyPropertySet.REALM_NAME));

            for (User user : getConfiguration().getUsers())
            {
                defaultRealm.getClass().getMethod("put", Object.class, Object.class).invoke(
                    defaultRealm, user.getName(), user.getPassword());

                for (String role : user.getRoles())
                {
                    defaultRealm.getClass().getMethod("addUserToRole", String.class, String.class)
                        .invoke(defaultRealm, user.getName(), role);
                }
            }

            // Add newly created realm to server
            getServer().getClass().getMethod("addRealm",
                getClassLoader().loadClass("org.mortbay.http.UserRealm"))
                    .invoke(getServer(), defaultRealm);
        }
    }

    /**
     * @param webapp the webapp to set the default security realm on
     * @throws Exception invokation error
     */
    protected void setDefaultRealm(Object webapp) throws Exception
    {
        if (this.defaultRealm != null)
        {
            webapp.getClass().getMethod("setRealm", this.defaultRealm.getClass())
                .invoke(webapp, this.defaultRealm);
        }
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
        return "Jetty 5.x Embedded";
    }
}
