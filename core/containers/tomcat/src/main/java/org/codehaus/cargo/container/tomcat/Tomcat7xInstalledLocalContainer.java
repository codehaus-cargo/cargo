/*
 * ========================================================================
 *
 * Copyright 2003-2008 The Apache Software Foundation. Code from this file 
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

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.tomcat.internal.AbstractCatalinaInstalledLocalContainer;

/**
 * Special container support for the Apache Tomcat 7.x servlet container.
 * 
 * @version $Id$
 */
public class Tomcat7xInstalledLocalContainer extends AbstractCatalinaInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "tomcat7x";

    /**
     * {@inheritDoc}
     * @see AbstractCatalinaInstalledLocalContainer#AbstractCatalinaInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Tomcat7xInstalledLocalContainer(LocalConfiguration configuration)
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
        return "Tomcat " + getVersion("7.x");
    }

    /**
     * Add the <code>tomcat-juli.jar</code> file to classpath and call parent.
     * 
     * @param action Either 'start' or 'stop'
     * @param java the prepared Ant Java command that will be executed
     * @exception Exception in case of container invocation error
     */
    @Override
    protected void invokeContainer(String action, JvmLauncher java) throws Exception
    {
        java.addClasspathEntries(new File(getHome(), "bin/tomcat-juli.jar"));
        super.invokeContainer(action, java);
    }
}
