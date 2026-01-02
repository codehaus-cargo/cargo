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
package org.codehaus.cargo.container.tomcat;


import java.io.File;
import java.lang.reflect.Method;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.tomcat.internal.TomcatEmbedded;
import org.codehaus.cargo.util.CargoException;

/**
 * Embedded Tomcat 9.x container.
 */
public class Tomcat9xEmbeddedLocalContainer extends Tomcat8xEmbeddedLocalContainer
{
    /**
     * Creates a Tomcat 9.x {@link org.codehaus.cargo.container.EmbeddedLocalContainer}.
     * 
     * @param configuration the configuration of the newly created container.
     */
    public Tomcat9xEmbeddedLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareController(TomcatEmbedded wrapper, File home, int port)
    {
        super.prepareController(wrapper, home, port);

        String maxPartCountStr =
            getConfiguration().getPropertyValue(TomcatPropertySet.CONNECTOR_MAX_PART_COUNT);
        if (maxPartCountStr != null)
        {
            int maxPartCount = Integer.parseInt(maxPartCountStr);
            try
            {
                Method method = connector.core.getClass().getMethod("setMaxPartCount", int.class);
                connector.invoke(method, maxPartCount);
            }
            catch (NoSuchMethodException | SecurityException e)
            {
                throw new CargoException("Cannot set maxPartCount", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId()
    {
        return "tomcat9x";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "Tomcat 9.x Embedded";
    }
}
