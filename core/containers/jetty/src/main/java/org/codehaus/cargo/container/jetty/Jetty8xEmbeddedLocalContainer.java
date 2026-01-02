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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * A Jetty 8.x instance running embedded.
 */
public class Jetty8xEmbeddedLocalContainer extends Jetty7xEmbeddedLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jetty8x";

    /**
     * {@inheritDoc}
     * @see Jetty7xEmbeddedLocalContainer#Jetty7xEmbeddedLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Jetty8xEmbeddedLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    @Override
    protected void doStart() throws Exception
    {
        createServerObject();
        configureJettyConnectors();
        setSecurityRealm();
        addJettyHandlers();
        addAnnotationConfiguration();
        addDeployables();
        startJetty();
    }

    /**
     * Add org.eclipse.jetty.annotations.AnnotationConfiguration to allow Servlet 3.0
     * ServletContainerInitializers to be found.
     * 
     * @throws IllegalAccessException thrown if the configuration could not be set
     * @throws InvocationTargetException thrown if the configuration could not be set
     * @throws NoSuchMethodException thrown if the configuration could not be set
     */
    protected void addAnnotationConfiguration() throws IllegalAccessException,
        InvocationTargetException, NoSuchMethodException
    {
        setAttributeMethod().invoke(
            getServer(),
            "org.eclipse.jetty.webapp.configuration",
                new String[] {
                    "org.eclipse.jetty.webapp.WebInfConfiguration",
                    "org.eclipse.jetty.webapp.WebXmlConfiguration",
                    "org.eclipse.jetty.webapp.MetaInfConfiguration",
                    "org.eclipse.jetty.webapp.FragmentConfiguration",
                    "org.eclipse.jetty.annotations.AnnotationConfiguration",
                    "org.eclipse.jetty.webapp.JettyWebXmlConfiguration"});
    }

    /**
     * Locate the method {@code org.eclipse.jetty.server.Server.setAttribute(String, Object)}
     * 
     * @return the setAttribute() method
     * @throws NoSuchMethodException thrown if the configuration could not be set
     */
    protected Method setAttributeMethod() throws NoSuchMethodException
    {
        return getServer().getClass().getMethod("setAttribute", String.class, Object.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId()
    {
        return ID;
    }
}
