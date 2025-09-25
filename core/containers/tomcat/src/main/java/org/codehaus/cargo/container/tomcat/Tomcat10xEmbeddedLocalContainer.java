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
package org.codehaus.cargo.container.tomcat;


import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.tomcat.internal.TomcatEmbedded;

/**
 * Embedded Tomcat 10.x container.
 */
public class Tomcat10xEmbeddedLocalContainer extends Tomcat9xEmbeddedLocalContainer
{
    /**
     * Creates a Tomcat 10.x {@link org.codehaus.cargo.container.EmbeddedLocalContainer}.
     * 
     * @param configuration the configuration of the newly created container.
     */
    public Tomcat10xEmbeddedLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId()
    {
        return "tomcat10x";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "Tomcat 10.x Embedded";
    }

    @Override
    protected void prepareController(TomcatEmbedded wrapper, File home, int port)
    {
        super.prepareController(wrapper, home, port);

        addExtraMaxPartCount();
    }

    private void addExtraMaxPartCount()
    {
        Map<String, String> props = this.getConfiguration().getProperties();
        if (props.containsKey(TomcatPropertySet.CONNECTOR_MAX_PART_COUNT))
        {
            String maxPartCountStr = props.get(TomcatPropertySet.CONNECTOR_MAX_PART_COUNT);
            int maxPartCount = Integer.parseInt(maxPartCountStr);
            try
            {
                Method method = connector.core.getClass().getMethod("setMaxPartCount", int.class);
                connector.invoke(method, maxPartCount);
            }
            catch (NoSuchMethodException | SecurityException e)
            {
                e.printStackTrace();
            }
        }
    }
}
