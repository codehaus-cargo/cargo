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

import java.io.FileNotFoundException;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.resin.internal.AbstractResinInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * Special container support for the Caucho Resin 3.x servlet container.
 * 
 */
public class Resin2xInstalledLocalContainer extends AbstractResinInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "resin2x";

    /**
     * {@inheritDoc}
     * @see AbstractResinInstalledLocalContainer#AbstractResinInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Resin2xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see AbstractResinInstalledLocalContainer#startUpAdditions(JvmLauncher)
     */
    @Override
    protected void startUpAdditions(JvmLauncher theJavaContainer)
        throws FileNotFoundException
    {
        // Nothing additional required
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public String getName()
    {
        return "Resin " + getVersion("2.x");
    }
}
