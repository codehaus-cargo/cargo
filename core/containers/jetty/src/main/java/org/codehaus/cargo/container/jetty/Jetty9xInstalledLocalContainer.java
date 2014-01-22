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
package org.codehaus.cargo.container.jetty;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * Special container support for the Jetty 9.x servlet container.
 * 
 * @version $Id$
 */
public class Jetty9xInstalledLocalContainer extends Jetty8xInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jetty9x";

    /**
     * Jetty9xInstalledLocalContainer Constructor.
     * @param configuration The configuration associated with the container
     */
    public Jetty9xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    @Override
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.jetty.Jetty6xInstalledLocalContainer#doStart(JvmLauncher)
     */
    @Override
    public void doStart(JvmLauncher java) throws Exception
    {
        String npnFolder = getFileHandler().append(getHome(), "modules/npn");
        if (getFileHandler().isDirectory(npnFolder))
        {
            String closest = "";
            String expected = "npn-" + System.getProperty("java.version") + ".mod";
            for (String npnFile : getFileHandler().getChildren(npnFolder))
            {
                String npnFilename = getFileHandler().getName(npnFile);
                int distanceClosest = Math.abs(closest.compareTo(expected));
                int distanceCurrent = Math.abs(npnFilename.compareTo(expected));
                if (distanceCurrent < distanceClosest)
                {
                    closest = npnFilename;
                }
            }
            if (closest.length() > 0 && !closest.equals(expected))
            {
                closest = getFileHandler().append(npnFolder, closest);
                expected = getFileHandler().append(npnFolder, expected);
                getFileHandler().copyFile(closest, expected);
            }
        }

        super.doStart(java);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.jetty.Jetty6xInstalledLocalContainer#getStartArguments(java.lang.String)
     */
    @Override
    protected String[] getStartArguments(String classpath)
    {
        if (getVersion().startsWith("9.0."))
        {
            return new String[]
            {
                getOptions(),
                "--ini",
                getFileHandler().append(getConfiguration().getHome(), "etc/jetty-logging.xml"),
                getFileHandler().append(getConfiguration().getHome(), "etc/jetty.xml"),
                getFileHandler().append(getConfiguration().getHome(), "etc/jetty-annotations.xml"),
                getFileHandler().append(getConfiguration().getHome(), "etc/jetty-http.xml"),
                getFileHandler().append(getConfiguration().getHome(), "etc/jetty-plus.xml"),
                getFileHandler().append(getConfiguration().getHome(), "etc/jetty-deploy.xml"),
                getFileHandler().append(getConfiguration().getHome(), "etc/test-realm.xml"),
                "path=" + classpath
            };
        }
        else
        {
            return new String[]
            {
                "--ini",
                "--module=logging",
                "--module=server",
                "--module=deploy",
                "--module=websocket",
                "--module=jsp",
                "--module=ext",
                "--module=resources",
                "--module=http",
                "--module=plus",
                "path=" + classpath
            };
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.jetty.Jetty7xInstalledLocalContainer#getStartArguments()
     */
    @Override
    protected String[] getStopArguments()
    {
        if (getVersion().startsWith("9.0."))
        {
            return super.getStopArguments();
        }
        else
        {
            return new String[]
            {
                "STOP.PORT=" + getConfiguration().getPropertyValue(GeneralPropertySet.RMI_PORT),
                "STOP.KEY=secret"
            };
        }
    }
}
