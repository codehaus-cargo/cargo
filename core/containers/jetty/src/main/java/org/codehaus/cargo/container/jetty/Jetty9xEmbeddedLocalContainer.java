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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.property.ServletPropertySet;

/**
 * A Jetty 9.x instance running embedded.
 */
public class Jetty9xEmbeddedLocalContainer extends Jetty8xEmbeddedLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jetty9x";

    /**
     * {@inheritDoc}
     * @see Jetty8xEmbeddedLocalContainer#Jetty8xEmbeddedLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Jetty9xEmbeddedLocalContainer(LocalConfiguration configuration)
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
    protected void configureJettyConnectors() throws ClassNotFoundException,
        InstantiationException, IllegalAccessException, InvocationTargetException,
        NoSuchMethodException
    {
        // Connector serverConnector = new ServerConnector(this.server);
        // serverConnector.setPort(new
        // Integer(getConfiguration().getPropertyValue(ServletPropertySet.PORT)));
        Class selectConnectorClass =
            getClassLoader().loadClass("org.eclipse.jetty.server.ServerConnector");
        Object connector = selectConnectorClass.getConstructor(this.server.getClass())
            .newInstance(this.server);
        selectConnectorClass.getMethod("setPort", int.class).invoke(connector,
            Integer.parseInt(getConfiguration().getPropertyValue(ServletPropertySet.PORT)));

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
    protected synchronized void createServerObject() throws Exception
    {
        if (this.server == null)
        {
            super.createServerObject();

            Class webAppContextClass =
                getClassLoader().loadClass("org.eclipse.jetty.webapp.WebAppContext");

            if (webAppContextClass.getPackage().getImplementationVersion() != null
                && (webAppContextClass.getPackage().getImplementationVersion().startsWith("9.3.")
                || webAppContextClass.getPackage().getImplementationVersion().startsWith("9.4.")))
            {
                // Override of the Jetty 9.3.x / 9.4.x server classes list, to work around the
                // nasty javax.servlet.ServletContainerInitializer error: Provider
                // org.eclipse.jetty.cdi.websocket.WebSocketCdiInitializer and/or
                // org.eclipse.jetty.cdi.CdiServletContainerInitializer not found.
                String[] dftServerClasses = (String[])
                    webAppContextClass.getDeclaredField("__dftServerClasses").get(null);
                List<String> dftServerClassesList =
                    new ArrayList<String>(dftServerClasses.length + 2);
                dftServerClassesList.add("-org.eclipse.jetty.cdi.");
                dftServerClassesList.add("-org.eclipse.jetty.cdi.websocket.");
                dftServerClassesList.addAll(Arrays.asList(dftServerClasses));
                dftServerClasses = new String[dftServerClassesList.size()];
                dftServerClasses = dftServerClassesList.toArray(dftServerClasses);
                server.getClass().getMethod("setAttribute", String.class, Object.class)
                    .invoke(server, "org.eclipse.jetty.webapp.serverClasses", dftServerClasses);

                String[] dftSystemClasses = (String[])
                    webAppContextClass.getDeclaredField("__dftSystemClasses").get(null);
                List<String> dftSystemClassesList =
                    new ArrayList<String>(dftSystemClasses.length + 2);
                dftSystemClassesList.addAll(Arrays.asList(dftSystemClasses));
                dftSystemClassesList.add("org.eclipse.jetty.cdi.");
                dftSystemClassesList.add("org.eclipse.jetty.cdi.websocket.");
                dftSystemClasses = new String[dftSystemClassesList.size()];
                dftSystemClasses = dftSystemClassesList.toArray(dftSystemClasses);
                server.getClass().getMethod("setAttribute", String.class, Object.class)
                    .invoke(server, "org.eclipse.jetty.webapp.systemClasses", dftSystemClasses);
            }
        }
    }
}
