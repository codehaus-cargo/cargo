/* 
 * ========================================================================
 * 
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
 * 
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
import java.io.FileNotFoundException;

import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.tomcat.internal.AbstractCatalinaInstalledLocalContainer;

/**
 * Special container support for the Apache Tomcat 4.x servlet container.
 * 
 * @version $Id$
 */
public class Tomcat4xInstalledLocalContainer extends AbstractCatalinaInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "tomcat4x";

    /**
     * {@inheritDoc}
     * @see AbstractCatalinaInstalledLocalContainer#AbstractCatalinaInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Tomcat4xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public final String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public final String getName()
    {
        return "Tomcat " + getVersion("4.x");
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractInstalledLocalContainer#doStart(Java)
     */
    @Override
    public void doStart(Java java) throws Exception
    {
        File commonLibDirectory = new File(getHome(), "common/lib");
        if (!commonLibDirectory.isDirectory())
        {
            throw new FileNotFoundException("directory " + commonLibDirectory + " does not exist");
        }
        Path classpath = java.getCommandLine().getClasspath();
        for (File commonLibrary : commonLibDirectory.listFiles())
        {
            classpath.createPathElement().setLocation(commonLibrary);
        }

        super.doStart(java);
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractLocalContainer#waitForCompletion(boolean)
     */
    @Override
    protected void waitForCompletion(boolean waitForStarting) throws InterruptedException
    {
        super.waitForCompletion(waitForStarting);

        if (!waitForStarting)
        {
            // Tomcat 4 stop is not synchronous, therefore sleep a bit after the
            // CARGO ping component has stopped in order to allow some time for
            // the server to stop completely
            Thread.sleep(10000);
        }
    }
}
