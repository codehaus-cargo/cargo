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

import java.lang.reflect.Method;

import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * A Jetty 10.x instance running embedded.
 */
public class Jetty10xEmbeddedLocalContainer extends Jetty9xEmbeddedLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jetty10x";

    /**
     * {@inheritDoc}
     * @see Jetty9xEmbeddedLocalContainer#Jetty9xEmbeddedLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Jetty10xEmbeddedLocalContainer(LocalConfiguration configuration)
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
    protected synchronized void createServerObject() throws Exception
    {
        if (this.server == null)
        {
            super.createServerObject();

            Class webAppContextClass =
                getClassLoader().loadClass(getWebappContextClassname());

            // Override of the Jetty 10.x server classes list, to work around the nasty
            // java.lang.ClassNotFoundException:
            // org.eclipse.jetty.servlet.listener.ELContextCleaner.
            Object dftServerClasses =
                webAppContextClass.getDeclaredField("__dftServerClasses").get(null);
            Method remove = dftServerClasses.getClass().getMethod("remove", Object.class);
            remove.invoke(dftServerClasses, "org.eclipse.jetty.");
        }
    }
}
