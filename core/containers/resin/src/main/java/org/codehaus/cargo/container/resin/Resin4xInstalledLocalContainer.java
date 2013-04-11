/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.container.resin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.resin.internal.AbstractResinInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * Special container support for the Caucho Resin 4.x servlet container.
 * 
 * @version $Id$
 */
public class Resin4xInstalledLocalContainer extends AbstractResinInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "resin4x";

    /**
     * {@inheritDoc}
     * @see AbstractResinInstalledLocalContainer#AbstractResinInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Resin4xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see AbstractResinInstalledLocalContainer#doStart(JvmLauncher)
     */
    @Override
    public void doStart(JvmLauncher java) throws Exception
    {
        java.addAppArguments("start");
        startUpAdditions(java);

        int returnCode = java.execute();
        if (returnCode != 0)
        {
            throw new ContainerException("Resin start command returned " + returnCode
                    + ", expected return code was 0");
        }
    }

    /**
     * {@inheritDoc}
     * @see AbstractResinInstalledLocalContainer#doStop(JvmLauncher)
     */
    @Override
    public void doStop(JvmLauncher java) throws Exception
    {
        java.addAppArguments("shutdown");
        startUpAdditions(java);
        java.execute();
    }

    /**
     * {@inheritDoc}
     * @see AbstractResinInstalledLocalContainer#startUpAdditions(JvmLauncher)
     */
    @Override
    protected void startUpAdditions(JvmLauncher java) throws FileNotFoundException
    {
        // It seems Resin 4.x requires the following property to be set in order to start
        java.setSystemProperty("java.util.logging.manager", "com.caucho.log.LogManagerImpl");

        JarInputStream jarStream = null;
        String classPath;
        String mainClass;
        try
        {
            jarStream = new JarInputStream(
                getFileHandler().getInputStream(
                    getFileHandler().append(getHome(), "lib/resin.jar")));
            Manifest mf = jarStream.getManifest();
            classPath = mf.getMainAttributes().getValue("Class-Path");
            mainClass = mf.getMainAttributes().getValue("Main-Class");
            jarStream.close();
        }
        catch (IOException e)
        {
            throw new ContainerException("Cannot read the resin JAR file", e);
        }
        finally
        {
            if (jarStream != null)
            {
                try
                {
                    jarStream.close();
                }
                catch (IOException ignored)
                {
                    // Ignored
                }
                jarStream = null;
                System.gc();
            }
        }

        File lib = new File(getHome(), "lib");
        java.addClasspathEntries(new File(lib, "resin.jar"));
        if (classPath != null)
        {
            for (String classPathElement : classPath.split("\\s"))
            {
                java.addClasspathEntries(new File(lib, classPathElement));
            }
        }

        if (mainClass == null)
        {
            throw new ContainerException("Cannot read the main class from the resin JAR file");
        }
        java.setMainClass(mainClass);

        // Invoke the main class to start the container
        java.addAppArguments("--resin-home");
        java.addAppArgument(new File(getHome()));
        java.addAppArguments("--root-directory");
        java.addAppArgument(new File(getConfiguration().getHome()));
        java.addAppArguments("--conf");
        java.addAppArgument(new File(getConfiguration().getHome(), "conf/resin.xml"));
        java.addAppArguments("--watchdog-port",
            getConfiguration().getPropertyValue(ResinPropertySet.SOCKETWAIT_PORT));
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
     * @see org.codehaus.cargo.container.Container#getName()
     */
    @Override
    public String getName()
    {
        return "Resin " + getVersion("4.x");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getResinConfigurationFileName()
    {
        return "resin.xml";
    }
}
