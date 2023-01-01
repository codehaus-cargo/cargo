/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
package org.codehaus.cargo.container.resin;

import java.io.File;
import java.io.FileNotFoundException;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.resin.internal.AbstractResinInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * Special container support for the Caucho Resin 3.x servlet container.
 */
public class Resin3xInstalledLocalContainer extends AbstractResinInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "resin3x";

    /**
     * {@inheritDoc}
     * @see AbstractResinInstalledLocalContainer#AbstractResinInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Resin3xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startUpAdditions(JvmLauncher java) throws FileNotFoundException
    {
        // It seems Resin 3.x requires the following property to be set in order to start
        java.setSystemProperty("java.util.logging.manager", "com.caucho.log.LogManagerImpl");

        // Add the bin directory to the library path so that the Resin dll/so can be loaded
        java.setSystemProperty("java.library.path", new File(getHome(), "bin").getAbsolutePath());

        java.addAppArguments("-socketwait");
        java.addAppArguments(
            getConfiguration().getPropertyValue(ResinPropertySet.SOCKETWAIT_PORT));

        // Add the tools.jar to the classpath.
        // This was not required for Resin 2.x but it is for Resin 3.x
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
        return "Resin " + getVersion("3.x");
    }
}
