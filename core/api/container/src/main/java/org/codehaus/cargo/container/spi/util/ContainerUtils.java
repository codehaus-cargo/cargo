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
package org.codehaus.cargo.container.spi.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.util.CargoException;

/**
 * Set of common Container utility methods for people extending Cargo.
 * 
 * @version $Id$
 */
public final class ContainerUtils
{
    /**
     * When we keep the container started, this is the time we wait before checking if the container
     * is stopped.
     */
    private static final int SLEEP = 100;

    /**
     * Ensures that this utility class cannot be instantiated.
     */
    private ContainerUtils()
    {
        // Do nothing...
    }

    /**
     * @param configuration the configuration from which to derive the CPC URL. We need it to get
     * the hostname, the port, etc.
     * @return the CPC Cargo URL
     */
    public static URL getCPCURL(Configuration configuration)
    {
        try
        {
            String hostname = configuration.getPropertyValue(GeneralPropertySet.HOSTNAME);
            if ("0.0.0.0".equals(hostname) || "::0".equals(hostname))
            {
                hostname = "localhost";
            }
            return new URL(configuration.getPropertyValue(GeneralPropertySet.PROTOCOL) + "://"
                + hostname + ":" + configuration.getPropertyValue(ServletPropertySet.PORT)
                + "/cargocpc/index.html");
        }
        catch (MalformedURLException e)
        {
            throw new ContainerException("Failed to compute CPC URL", e);
        }
    }

    /**
     * Wait indefinitely till the container is stopped.
     * 
     * @param container the local container
     */
    public static void waitTillContainerIsStopped(Container container)
    {
        while (container.getState() == State.STARTED)
        {
            try
            {
                Thread.sleep(SLEEP);
            }
            catch (InterruptedException e)
            {
                throw new CargoException("Aborting container wait.", e);
            }
        }
    }
}
