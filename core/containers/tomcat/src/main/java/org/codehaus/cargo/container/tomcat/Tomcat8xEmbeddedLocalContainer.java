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
package org.codehaus.cargo.container.tomcat;

import java.io.File;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.tomcat.internal.AbstractCatalinaEmbeddedLocalContainer;
import org.codehaus.cargo.container.tomcat.internal.TomcatEmbedded;
import org.codehaus.cargo.container.tomcat.internal.TomcatEmbedded.MemoryRealm;

/**
 * Embedded Tomcat 8.x container.
 * 
 * @version $Id$
 */
public class Tomcat8xEmbeddedLocalContainer extends AbstractCatalinaEmbeddedLocalContainer
{
    /**
     * Classloader to use for loading the Embedded container's classes.
     */
    private static ClassLoader classLoader;

    /**
     * Creates a Tomcat 8.x {@link org.codehaus.cargo.container.EmbeddedLocalContainer}.
     * 
     * @param configuration the configuration of the newly created container.
     */
    public Tomcat8xEmbeddedLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public String getId()
    {
        return "tomcat8x";
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public String getName()
    {
        return "Tomcat 8.x Embedded";
    }

    /**
     * {@inheritDoc}
     */
    protected void prepareController(TomcatEmbedded wrapper, File home, int port)
    {
        controller.enableNaming();

        TomcatEmbedded.Engine engine = controller.getEngine();

        engine.setParentClassLoader(getClassLoader());

        TomcatEmbedded.MemoryRealm realm = wrapper.new MemoryRealm();
        realm.setPathname(new File(home, "conf/tomcat-users.xml"));
        engine.setRealm(realm);

        controller.setPort(port);
        connector = controller.getConnector();

        host = controller.getHost();
        host.setAutoDeploy(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStop() throws Exception
    {
        TomcatEmbedded.Context[] contexts = null;
        if (host != null)
        {
            host.findChildren();
        }

        super.doStop();

        if (contexts != null)
        {
            for (TomcatEmbedded.Context context : contexts)
            {
                context.destroy();
            }
        }
    }

    /**
     * Tomcat 8.x has a weird class called TomcatURLStreamHandlerFactory where the singleton has a
     * static <code>instance</code> field and a final attribute <code>registered</code> which are
     * not always in sync and cause unexpected exceptions. Save old class loaders in order to avoid
     * trouble when the container is executed twice (for example, in CARGO's integration tests).
     * {@inheritDoc}
     */
    @Override
    public ClassLoader getClassLoader()
    {
        if (Tomcat8xEmbeddedLocalContainer.classLoader == null)
        {
            Tomcat8xEmbeddedLocalContainer.classLoader = super.getClassLoader();
        }
        return Tomcat8xEmbeddedLocalContainer.classLoader;
    }
}
