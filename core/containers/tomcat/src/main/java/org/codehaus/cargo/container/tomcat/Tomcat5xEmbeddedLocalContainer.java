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

/**
 * Embedded Tomcat 5.x container.
 * 
 * @version $Id$
 */
public class Tomcat5xEmbeddedLocalContainer extends AbstractCatalinaEmbeddedLocalContainer
{
    /**
     * Creates a Tomcat 5.x {@link org.codehaus.cargo.container.EmbeddedLocalContainer}.
     * 
     * @param configuration the configuration of the newly created container.
     */
    public Tomcat5xEmbeddedLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public String getId()
    {
        return "tomcat5x";
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public String getName()
    {
        return "Tomcat 5.x Embedded";
    }

    /**
     * {@inheritDoc}
     */
    protected void prepareController(TomcatEmbedded wrapper, File home, int port)
    {
        controller.setCatalinaHome(new File(getConfiguration().getHome()));

        // unless we set this explicitly, it will be loaded from CATALINA_BASE
        TomcatEmbedded.MemoryRealm realm = wrapper.new MemoryRealm();
        realm.setPathname(new File(home, "conf/tomcat-users.xml"));
        controller.setRealm(realm);

        // no easy way to do this with reflection
        // controller.setLogger(new LoggerAdapter(getLogger()));

        TomcatEmbedded.Engine engine = controller.createEngine();
        engine.setName("engine");
        engine.setBaseDir(home.getPath());
        engine.setParentClassLoader(getClassLoader());

        // create just one Host
        host = controller.createHost("localhost", new File(home,
            getConfiguration().getPropertyValue(TomcatPropertySet.WEBAPPS_DIRECTORY)));
        host.setAutoDeploy(false);
        engine.addChild(host);
        engine.setDefaultHost(host.getName());

        // publish engine
        controller.addEngine(engine);

        // create HTTP connector
        connector = controller.createConnector(null, port, false);
        controller.addConnector(connector);
    }
}
