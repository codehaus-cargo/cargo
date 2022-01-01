/*
 * ========================================================================
 *
 * Copyright 2003-2008 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
package org.codehaus.cargo.container.jrun;

import java.io.File;
import java.io.FileNotFoundException;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.jrun.internal.AbstractJRunInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * Special container support for the Adobe JRun4.x servlet container.
 */
public class JRun4xInstalledLocalContainer extends AbstractJRunInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jrun4x";

    /**
     * {@inheritDoc}
     * @see AbstractJRunInstalledLocalContainer#AbstractJRunInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public JRun4xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startUpAdditions(JvmLauncher java) throws FileNotFoundException
    {
        java.setSystemProperty("sun.io.useCanonCaches", "false");
        java.setSystemProperty("jmx.invoke.getters", "true");

        // If getHome() contains spaces a hot fix is required in order for jrun to be able to
        // stop itself. The following property is needed along with the hot fix.
        // see: http://kb.adobe.com/selfservice/viewContent.do?externalId=4c7d1c1
        File hotFixJar = new File(getHome() + "/servers/lib/54101.jar");
        if (hotFixJar.exists())
        {
            java.setSystemProperty("-Djava.rmi.server.RMIClassLoaderSpi",
                "jrunx.util.JRunRMIClassLoaderSpi");
        }

        java.setSystemProperty("java.home", System.getProperty("java.home"));

        // Add the tools.jar to the classpath.
        addToolsJarToClasspath(java);
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
        return "JRun " + getVersion("4.x");
    }
}
